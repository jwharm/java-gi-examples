/* SVG Spacewar is copyright (C) 2005 by Nigel Tao: nigel.tao@myrealbox.com
 * This port to Gtk 4 and Kotlin is copyright (C) 2024 Jan-Willem Harmannij
 *
 * SPDX-License-Identifier: GPL-2.0-or-later
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package io.github.jwharm.javagi.examples

import io.github.jwharm.javagi.gobject.types.Types
import org.freedesktop.cairo.*
import org.gnome.gdk.*
import org.gnome.gio.ApplicationFlags
import org.gnome.glib.GLib
import org.gnome.glib.Type
import org.gnome.gobject.GObject
import org.gnome.graphene.Rect
import org.gnome.gtk.Application
import org.gnome.gtk.ApplicationWindow
import org.gnome.gtk.EventControllerKey
import org.gnome.gtk.Picture
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import kotlin.math.*
import kotlin.random.Random

// Global constants

const val WIDTH = 800
const val HEIGHT = 600

const val TWO_PI = 2 * PI

// trig computations (and x, y, velocity, etc). are made in fixed point arithmetic
const val FIXED_POINT_SCALE_FACTOR = 1024
const val FIXED_POINT_HALF_SCALE_FACTOR = 32

// discretization of 360 degrees
const val NUMBER_OF_ROTATION_ANGLES = 60
const val RADIANS_PER_ROTATION_ANGLE = TWO_PI / NUMBER_OF_ROTATION_ANGLES

// equivalent to 25 fps
const val MILLIS_PER_FRAME = 60

// a shot every 9/25 seconds = 8 ticks between shots
const val TICKS_BETWEEN_FIRE = 8

// fudge this for bigger or smaller ships
const val GLOBAL_SHIP_SCALE_FACTOR = 0.8

const val SHIP_ACCELERATION_FACTOR = 1
const val SHIP_MAX_VELOCITY = 10 * FIXED_POINT_SCALE_FACTOR
const val SHIP_RADIUS = 38 * FIXED_POINT_SCALE_FACTOR * GLOBAL_SHIP_SCALE_FACTOR

const val SHIP_MAX_ENERGY = 1000
const val DAMAGE_PER_MISSILE = 200
const val ENERGY_PER_MISSILE = 10

// bounce damage depends on how fast you're going
const val DAMAGE_PER_SHIP_BOUNCE_DIVISOR = 30

const val NUMBER_OF_STARS = 20

const val MAX_NUMBER_OF_MISSILES = 60

const val MISSILE_RADIUS = 4.0 * FIXED_POINT_SCALE_FACTOR
const val MISSILE_SPEED = 8
const val MISSILE_TICKS_TO_LIVE = 60
const val MISSILE_EXPLOSION_TICKS_TO_LIVE = 6

// Data classes

data class RGB(
    var r: Double = 0.0,
    var g: Double = 0.0,
    var b: Double = 0.0
)

data class Physics(
    var x: Double = 0.0,
    var y: Double = 0.0,

    var vx: Double = 0.0,
    var vy: Double = 0.0,

    // 0 is straight up, (NUMBER_OF_ROTATION_ANGLES / 4) is pointing right
    var rotation: Int = 0,

    // used for collision detection - we presume that an object is equivalent
    // to its bounding circle, rather than trying to do something fancy.
    var radius: Double = 0.0
)

data class Player(
    var p: Physics = Physics(),

    var thrusting: Boolean = false,
    var turningLeft: Boolean = false,
    var turningRight: Boolean = false,
    var firing: Boolean = false,

    var primaryColor: RGB = RGB(),
    var secondaryColor: RGB = RGB(),

    var ticksUntilCanFire: Int = 0,
    var energy: Int = 0,

    var hit: Boolean = false,
    var dead: Boolean = false
)

data class Missile(
    var p: Physics = Physics(),

    var alive: Boolean = false,

    var primaryColor: RGB = RGB(),
    var secondaryColor: RGB = RGB(),

    var ticksToLive: Int = 0,
    var exploded: Boolean = false
)

data class Star(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var rotation: Double = 0.0,
    var scale: Double = 0.0
)

// Players

var player1 = Player()
var player2 = Player()

// Missiles

var missiles = Array(MAX_NUMBER_OF_MISSILES) { Missile() }
var nextMissileIndex = 0

fun initMissilesArray() {
    for (missile in missiles) {
        missile.p.radius = MISSILE_RADIUS
        missile.alive = false
    }
}

// Stars

var stars = Array(NUMBER_OF_STARS) { Star() }

fun initStarsArray() {
    for (star in stars) {
        star.x = Random.nextInt(0, WIDTH).toDouble()
        star.y = Random.nextInt(0, HEIGHT).toDouble()
        star.rotation = Random.nextDouble() * TWO_PI
        star.scale = 0.5 + Random.nextDouble()
    }
}

// Global variables

const val showFPS = true
var debugScaleFactor = 1.0
var numberOfFrames = 0
var millisTakenForFrames = 0L
var gameOverMessage: String? = null
var quit = false

// Trigonometric tables

var cosTable = Array(NUMBER_OF_ROTATION_ANGLES) { 0.0 }
var sinTable = Array(NUMBER_OF_ROTATION_ANGLES) { 0.0 }

fun initTrigonometricTables() {
    val q = NUMBER_OF_ROTATION_ANGLES / 4

    for (i in cosTable.indices) {
        // our angle system is "true north" - 0 is straight up, whereas
        // cos & sin take 0 as east (and in radians).
        val angleInRadians = (q - i) * TWO_PI / NUMBER_OF_ROTATION_ANGLES
        cosTable[i] = cos(angleInRadians) * FIXED_POINT_SCALE_FACTOR

        // also, our graphics system is "y axis down", although in regular math,
        // the y axis is "up", so we have to multiply sin by -1.
        sinTable[i] = -sin(angleInRadians) * FIXED_POINT_SCALE_FACTOR
    }
}

/**
 * Main method: Create the UI and run the main loop
 */
fun main(args: Array<String>) {
    initTrigonometricTables()
    reset()

    val app = Application(
        "io.github.jwharm.javagi.examples.spacewar",
        setOf(ApplicationFlags.DEFAULT_FLAGS)
    )

    app.onActivate {
        val controller = EventControllerKey()
        controller.onKeyPressed { keyval, _, _ -> onKeyEvent(keyval, true); true }
        controller.onKeyReleased { keyval, _, _ -> onKeyEvent(keyval, false) }

        val paintable = SpacewarPaintable.create()
        val picture = Picture.builder()
            .setHexpand(true)
            .setVexpand(true)
            .setPaintable(paintable)
            .build()

        val window = ApplicationWindow.builder()
            .setApplication(app)
            .setDefaultWidth(WIDTH)
            .setDefaultHeight(HEIGHT)
            .setTitle("Spacewar")
            .setChild(picture)
            .build()
        window.addController(controller)
        window.present()

        // Main loop
        GLib.timeoutAdd(GLib.PRIORITY_DEFAULT, MILLIS_PER_FRAME) {
            onTimeout()
            paintable.invalidateContents()

            if (quit)
                app.quit()

            GLib.SOURCE_CONTINUE
        }
    }

    app.run(args)
}

/**
 * GdkPaintable implementation for drawing snapshots
 */
class SpacewarPaintable : GObject, Paintable {

    override fun snapshot(snapshot: Snapshot, width: Double, height: Double) {
        val startTime = if (showFPS) System.currentTimeMillis() else 0L
        val rect = Rect.alloc().init(0f, 0f, width.toFloat(), height.toFloat())
        val cr = (snapshot as org.gnome.gtk.Snapshot).appendCairo(rect)

        cr.save()
            .scaleForAspectRatio(width, height)
            .scale(debugScaleFactor, debugScaleFactor)

            // draw background space color
            .setSourceRGB(0.1, 0.0, 0.1)
            .paint()

        // draw any stars...
        for (star in stars) {
            cr.save()
                .translate(star.x, star.y)
                .rotate(star.rotation)
                .scale(star.scale, star.scale)
                .drawStar()
                .restore()
        }

        // ... the energy bars...
        cr.save()
            .translate(30.0, 30.0)
            .rotate(0.0)
            .drawEnergyBar(player1)
            .restore()

        cr.save()
            .translate((WIDTH - 30.0), 30.0)
            .rotate(PI)
            .drawEnergyBar(player2)
            .restore()

        // ... the two ships...
        cr.save()
            .translate(player1.p.x / FIXED_POINT_SCALE_FACTOR, player1.p.y / FIXED_POINT_SCALE_FACTOR)
            .rotate(player1.p.rotation * RADIANS_PER_ROTATION_ANGLE)
            .drawShipBody(player1)
            .restore()

        cr.save()
            .translate(player2.p.x / FIXED_POINT_SCALE_FACTOR, player2.p.y / FIXED_POINT_SCALE_FACTOR)
            .rotate(player2.p.rotation * RADIANS_PER_ROTATION_ANGLE)
            .drawShipBody(player2)
            .restore()

        // ... and any missiles.
        for (missile in missiles)
            if (missile.alive)
                cr.save()
                    .translate(missile.p.x / FIXED_POINT_SCALE_FACTOR, missile.p.y / FIXED_POINT_SCALE_FACTOR)
                    .rotate(missile.p.rotation * RADIANS_PER_ROTATION_ANGLE)
                    .drawMissile(missile)
                    .restore()

        if (gameOverMessage == null)
            gameOverMessage = if (player1.dead)
                if (player2.dead) "DRAW" else "RED wins"
            else
                if (player2.dead) "BLUE wins" else null
        if (gameOverMessage != null) {
            cr.showTextMessage(80, -30, gameOverMessage)
            cr.showTextMessage(30, 40, "Press [SPACE] to restart")
        }

        cr.restore()

        if (showFPS) {
            numberOfFrames++
            millisTakenForFrames += System.currentTimeMillis() - startTime
            if (numberOfFrames >= 100) {
                val fps = 1000.0 * (numberOfFrames.toDouble() / millisTakenForFrames.toDouble())
                println("$numberOfFrames frames in ${millisTakenForFrames}ms (${fps.toInt()}fps)")
                numberOfFrames = 0
                millisTakenForFrames = 0
            }
        }
    }

    override fun getCurrentImage(): Paintable {
        return this
    }

    constructor(address: MemorySegment?) : super(address)
    constructor() : super(gtype, null)

    companion object {
        private val gtype: Type = Types.register<SpacewarPaintable, ObjectClass>(SpacewarPaintable::class.java)

        fun create(): SpacewarPaintable {
            return newInstance(gtype)
        }
    }
}

/**
 * Scale the output to the size of the window
 */
fun Context.scaleForAspectRatio(widgetWidth: Double, widgetHeight: Double): Context {
    val isWidgetWider = (widgetWidth * HEIGHT) > (WIDTH * widgetHeight)
    val scale: Double
    val playfieldWidth: Double
    val playfieldHeight: Double
    val tx: Double
    val ty: Double

    if (isWidgetWider) {
        scale = widgetHeight / HEIGHT
        playfieldWidth = (WIDTH * widgetHeight) / HEIGHT
        playfieldHeight = widgetHeight
        tx = (widgetWidth - playfieldWidth) / 2
        ty = 0.0
    } else {
        scale = widgetWidth / WIDTH
        playfieldWidth = widgetWidth
        playfieldHeight = (HEIGHT * widgetWidth) / WIDTH
        tx = 0.0
        ty = (widgetHeight - playfieldHeight) / 2
    }

    translate(tx, ty)
    rectangle(0.0, 0.0, playfieldWidth, playfieldHeight)
    clip()
    scale(scale, scale)

    return this
}

/**
 * Draw the energy bar for a player
 */
fun Context.drawEnergyBar(p: Player): Context {
    val alpha = 0.6

    save()
    rectangle(0.0, -5.0, p.energy / 5.0, 10.0)

    val pat = LinearGradient.create(0.0, 0.0, SHIP_MAX_ENERGY / 5.0, 0.0)
    pat.addColorStopRGBA(0.0, p.secondaryColor.r, p.secondaryColor.g, p.secondaryColor.b, alpha)
    pat.addColorStopRGBA(1.0, p.primaryColor.r, p.primaryColor.g, p.primaryColor.b, alpha)

    setSource(pat)
    fillPreserve()
    setSourceRGB(0.0, 0.0, 0.0)
    stroke()
    restore()

    return this
}

/**
 * Draw operations for a spaceship
 */
fun Context.drawShipBody(p: Player): Context {
    if (p.hit) {
        setSourceRGBA(p.primaryColor.r, p.primaryColor.g, p.primaryColor.b, 0.5)
        arc(0.0, 0.0, SHIP_RADIUS / FIXED_POINT_SCALE_FACTOR, 0.0, TWO_PI)
        stroke()
    }

    save()
    scale(GLOBAL_SHIP_SCALE_FACTOR, GLOBAL_SHIP_SCALE_FACTOR)

    if (!p.dead) {
        if (p.thrusting)
            drawFlare(p.primaryColor)
        if (p.turningLeft)
            drawTurningFlare(p.primaryColor, -1.0)
        if (p.turningRight)
            drawTurningFlare(p.primaryColor, 1.0)
    }

    moveTo(0.0, -33.0)
    curveTo(2.0, -33.0, 3.0, -34.0, 4.0, -35.0)
    curveTo(8.0, -10.0, 6.0, 15.0, 15.0, 15.0)
    lineTo(20.0, 15.0)
    lineTo(20.0, 7.0)
    curveTo(25.0, 10.0, 28.0, 22.0, 25.0, 28.0)
    curveTo(20.0, 26.0, 8.0, 24.0, 0.0, 24.0)

    // half way point
    curveTo(-8.0, 24.0, -20.0, 26.0, -25.0, 28.0)
    curveTo(-28.0, 22.0, -25.0, 10.0, -20.0, 7.0)
    lineTo(-20.0, 15.0)
    lineTo(-15.0, 15.0)
    curveTo(-6.0, 15.0, -8.0, -10.0, -4.0, -35.0)
    curveTo(-3.0, -34.0, -2.0, -33.0, 0.0, -33.0)

    val pat = LinearGradient.create(-30.0, -30.0, 30.0, 30.0)
    pat.addColorStopRGBA(0.0, p.primaryColor.r, p.primaryColor.g, p.primaryColor.b, 1.0)
    pat.addColorStopRGBA(1.0, p.secondaryColor.r, p.secondaryColor.g, p.secondaryColor.b, 1.0)

    setSource(pat)
    fillPreserve()

    setSourceRGB(0.0, 0.0, 0.0)
    stroke()
    restore()

    return this
}

/**
 * Draw operations for a flare when thrusting forward
 */
fun Context.drawFlare(color: RGB): Context {
    save()
    translate(0.0, 22.0)
    val pat = RadialGradient.create(0.0, 0.0, 2.0, 0.0, 5.0, 12.0)
    pat.addColorStopRGBA(0.0, color.r, color.g, color.b, 1.0)
    pat.addColorStopRGBA(0.3, 1.0, 1.0, 1.0, 1.0)
    pat.addColorStopRGBA(1.0, color.r, color.g, color.b, 0.0)
    setSource(pat)
    arc(0.0, 0.0, 20.0, 0.0, TWO_PI)
    fill()
    restore()

    return this
}

/**
 * Draw operations for a flare when turning
 */
fun Context.drawTurningFlare(color: RGB, rightHandSide: Double): Context {
    val pat = RadialGradient.create(0.0, 0.0, 1.0, 0.0, 0.0, 7.0)
    pat.addColorStopRGBA(0.0, 1.0, 1.0, 1.0, 1.0)
    pat.addColorStopRGBA(1.0, color.r, color.b, color.g, 0.0)

    save()
    translate(-23 * rightHandSide, 28.0)
    setSource(pat)
    arc(0.0, 0.0, 7.0, 0.0, TWO_PI)
    fill()

    translate(42 * rightHandSide, -22.0)
    setSource(pat)
    arc(0.0, 0.0, 5.0, 0.0, TWO_PI)
    fill()
    restore()

    return this
}

/**
 * Draw operations for a missile
 */
fun Context.drawMissile(m: Missile): Context {
    save()
    scale(GLOBAL_SHIP_SCALE_FACTOR, GLOBAL_SHIP_SCALE_FACTOR)

    if (m.exploded) {
        drawExplodedMissile(m)
    } else {
        var alpha = m.ticksToLive.toDouble() / MISSILE_TICKS_TO_LIVE
        // non-linear scaling so things don't fade out too fast
        alpha = 1.0 - (1.0 - alpha) * (1.0 - alpha)

        val pat = LinearGradient.create(0.0, -5.0, 0.0, 5.0)
        pat.addColorStopRGBA(0.0, m.primaryColor.r, m.primaryColor.g, m.primaryColor.b, alpha)
        pat.addColorStopRGBA(1.0, m.secondaryColor.r, m.secondaryColor.g, m.secondaryColor.b, alpha)

        save()
        moveTo(0.0, -4.0)
        curveTo(3.0, -4.0, 4.0, -2.0, 4.0, 0.0)
        curveTo(4.0, 4.0, 2.0, 10.0, 0.0, 18.0)
        // half way point
        curveTo(-2.0, 10.0, -4.0, 4.0, -4.0, 0.0)
        curveTo(-4.0, -2.0, -3.0, -4.0, 0.0, -4.0)

        setSource(pat)
        fill()
        restore()

        save()
        arc(0.0, 0.0, 3.0, 0.0, TWO_PI)
        setSource(pat)
        fill()
        restore()
    }

    restore()

    return this
}

/**
 * Draw operations for a missile explosion
 */
fun Context.drawExplodedMissile(m: Missile): Context {
    save()
    scale(GLOBAL_SHIP_SCALE_FACTOR, GLOBAL_SHIP_SCALE_FACTOR)

    var alpha = m.ticksToLive.toDouble() / MISSILE_EXPLOSION_TICKS_TO_LIVE
    alpha = 1.0 - (1.0 - alpha) * (1.0 - alpha)

    arc(0.0, 0.0, 30.0, 0.0, TWO_PI)

    val pat = RadialGradient.create(0.0, 0.0, 0.0, 0.0, 0.0, 30.0)
    pat.addColorStopRGBA(0.0, m.primaryColor.r, m.primaryColor.g, m.primaryColor.b, alpha)
    pat.addColorStopRGBA(0.5, m.secondaryColor.r, m.secondaryColor.g, m.secondaryColor.b, alpha * 0.75)
    pat.addColorStopRGBA(1.0, 0.0, 0.0, 0.0, 0.0)

    setSource(pat)
    fill()
    restore()

    return this
}

/**
 * Draw operations for a star
 */
fun Context.drawStar(): Context {
    val a = NUMBER_OF_ROTATION_ANGLES / 10
    val r1 = 5.0
    val r2 = 2.0

    save()
    moveTo(r1 * cosTable[0] / FIXED_POINT_SCALE_FACTOR, r1 * sinTable[0] / FIXED_POINT_SCALE_FACTOR)

    for (i in 0..4) {
        lineTo(r1 * cosTable[0] / FIXED_POINT_SCALE_FACTOR, r1 * sinTable[0] / FIXED_POINT_SCALE_FACTOR)
        lineTo(r2 * cosTable[a] / FIXED_POINT_SCALE_FACTOR, r2 * sinTable[a] / FIXED_POINT_SCALE_FACTOR)
        rotate(4 * a * PI / NUMBER_OF_ROTATION_ANGLES)
    }

    closePath()
    restore()

    val c = 0.5
    setSourceRGB(c, c, c)
    fill()

    return this
}

/**
 * Main game loop
 */
fun onTimeout() {
    player1.hit = false
    player2.hit = false

    applyPhysicsToPlayer(player1)
    applyPhysicsToPlayer(player2)

    if (checkForCollision(player1.p, player2.p)) {
        enforceMinimumDistance(player1.p, player2.p)

        val p1vx = player1.p.vx
        val p1vy = player1.p.vy
        val p2vx = player2.p.vx
        val p2vy = player2.p.vy

        val dvx = (p1vx - p2vx) / FIXED_POINT_HALF_SCALE_FACTOR
        val dvy = (p1vy - p2vy) / FIXED_POINT_HALF_SCALE_FACTOR
        val dv2 = (dvx * dvx) + (dvy * dvy)
        val damage = sqrt(dv2).toInt() / DAMAGE_PER_SHIP_BOUNCE_DIVISOR

        player1.energy -= damage
        player2.energy -= damage
        player1.hit = true
        player2.hit = true

        player1.p.vx = (p1vx * -2 / 8) + (p2vx * 5 / 8)
        player1.p.vy = (p1vx * -2 / 8) + (p2vx * 5 / 8)
        player2.p.vx = (p1vx * 5 / 8) + (p2vx * -2 / 8)
        player2.p.vy = (p1vx * 5 / 8) + (p2vx * -2 / 8)
    }

    for (m in missiles) {
        if (m.alive) {
            applyPhysics(m.p)

            if (!m.exploded) {
                if (checkForCollision(m.p, player1.p))
                    onCollision(player1, m)
                if (checkForCollision(m.p, player2.p))
                    onCollision(player2, m)
            }

            m.ticksToLive--
            if (m.ticksToLive <= 0)
                m.alive = false
        }
    }

    if (player1.energy <= 0) {
        player1.energy = 0
        player1.dead = true
    } else {
        player1.energy = min(SHIP_MAX_ENERGY, player1.energy + 1)
    }

    if (player2.energy <= 0) {
        player2.energy = 0
        player2.dead = true
    } else {
        player2.energy = min(SHIP_MAX_ENERGY, player2.energy + 1)
    }
}

/**
 * Adjust player direction and velocity, fire a new missile, apply physics
 */
fun applyPhysicsToPlayer(player: Player) {
    val p: Physics = player.p

    if (!player.dead) {

        // check if player is turning left, ...
        if (player.turningLeft) {
            p.rotation--
            while (p.rotation < 0)
                p.rotation += NUMBER_OF_ROTATION_ANGLES
        }

        // ... or right.
        if (player.turningRight) {
            p.rotation++
            while (p.rotation >= NUMBER_OF_ROTATION_ANGLES)
                p.rotation -= NUMBER_OF_ROTATION_ANGLES
        }

        // check if accelerating
        if (player.thrusting) {
            p.vx += SHIP_ACCELERATION_FACTOR * cosTable[p.rotation]
            p.vy += SHIP_ACCELERATION_FACTOR * sinTable[p.rotation]
        }

        // apply velocity upper bound
        val v2 = ((p.vx) * (p.vx)) + ((p.vy) * (p.vy))
        val m2 = SHIP_MAX_VELOCITY * SHIP_MAX_VELOCITY
        if (v2 > m2) {
            p.vx = p.vx * m2 / v2
            p.vy = p.vy * m2 / v2
        }

        // check if player is shooting
        if (player.ticksUntilCanFire == 0) {
            if (player.firing && player.energy > ENERGY_PER_MISSILE) {
                val xx = cosTable[p.rotation]
                val yy = sinTable[p.rotation]

                val m = missiles[nextMissileIndex++]

                player.energy -= ENERGY_PER_MISSILE

                if (nextMissileIndex == MAX_NUMBER_OF_MISSILES)
                    nextMissileIndex = 0

                m.p.x = p.x + (((SHIP_RADIUS + MISSILE_RADIUS) / FIXED_POINT_SCALE_FACTOR) * xx)
                m.p.y = p.y + (((SHIP_RADIUS + MISSILE_RADIUS) / FIXED_POINT_SCALE_FACTOR) * yy)
                m.p.vx = p.vx + (MISSILE_SPEED * xx)
                m.p.vy = p.vy + (MISSILE_SPEED * yy)
                m.p.rotation = p.rotation
                m.ticksToLive = MISSILE_TICKS_TO_LIVE
                m.primaryColor = player.primaryColor
                m.secondaryColor = player.secondaryColor
                m.alive = true
                m.exploded = false

                player.ticksUntilCanFire += TICKS_BETWEEN_FIRE
            }
        } else {
            player.ticksUntilCanFire--
        }
    }

    // apply velocity deltas to displacement
    applyPhysics(p)
}

/**
 * Adjust x and y coordinates based on velocity
 */
fun applyPhysics(p: Physics) {
    p.x += p.vx
    while (p.x > (WIDTH * FIXED_POINT_SCALE_FACTOR))
        p.x -= (WIDTH * FIXED_POINT_SCALE_FACTOR)
    while (p.x < 0)
        p.x += (WIDTH * FIXED_POINT_SCALE_FACTOR)

    p.y += p.vy
    while (p.y > (HEIGHT * FIXED_POINT_SCALE_FACTOR))
        p.y -= (HEIGHT * FIXED_POINT_SCALE_FACTOR)
    while (p.y < 0)
        p.y += (HEIGHT * FIXED_POINT_SCALE_FACTOR)
}

/**
 * Return true when both objects collide
 */
fun checkForCollision(p1: Physics, p2: Physics): Boolean {
    val dx = (p1.x - p2.x) / FIXED_POINT_HALF_SCALE_FACTOR
    val dy = (p1.y - p2.y) / FIXED_POINT_HALF_SCALE_FACTOR
    val r = (p1.radius + p2.radius) / FIXED_POINT_HALF_SCALE_FACTOR
    val d2 = (dx * dx) + (dy * dy)
    return d2 < (r * r)
}

/**
 * Bounce objects when too close
 */
fun enforceMinimumDistance(p1: Physics, p2: Physics) {
    var dx = p1.x - p2.x
    var dy = p1.y - p2.y
    val d2 = (dx * dx) + (dy * dy)
    val d = sqrt(d2).toInt()

    val r = p1.radius + p2.radius

    // normalize dx and dy to length = ((r - d) / 2) + fudge_factor
    val desiredVectorLength = ((r - d) * 5) / 8

    dx *= desiredVectorLength
    dy *= desiredVectorLength
    dx /= d
    dy /= d

    p1.x += dx
    p1.y += dy
    p2.x -= dx
    p2.y -= dy
}

/**
 * Apply damage to player and explode the missile
 */
fun onCollision(p: Player, m: Missile) {
    p.energy -= DAMAGE_PER_MISSILE
    p.hit = true
    m.exploded = true
    m.ticksToLive = MISSILE_EXPLOSION_TICKS_TO_LIVE
    m.p.vx = 0.0
    m.p.vy = 0.0
}

/**
 * Display text message with the requested size
 */
fun Context.showTextMessage(fontSize: Int, dy: Int, message: String?): Context {
    val extents = TextExtents.create(Arena.ofAuto())
    save()

    selectFontFace("Serif", FontSlant.NORMAL, FontWeight.NORMAL)
    setFontSize(fontSize.toDouble())
    textExtents(message, extents)
    val x = (WIDTH / 2) - (extents.width() / 2 + extents.xBearing())
    val y = (HEIGHT / 2) - (extents.height() / 2 + extents.yBearing())

    setSourceRGBA(1.0, 1.0, 1.0, 1.0)
    moveTo(x, y + dy)
    showText(message)
    restore()

    return this
}

/**
 * Reset to initial game state
 */
fun reset() {
    player1.p.x = 200.0 * FIXED_POINT_SCALE_FACTOR
    player1.p.y = 200.0 * FIXED_POINT_SCALE_FACTOR
    player1.p.vx = 0.0
    player1.p.vy = 0.0
    player1.p.rotation = Random.nextInt(0, NUMBER_OF_ROTATION_ANGLES - 1)
    player1.p.radius = SHIP_RADIUS
    player1.thrusting = false
    player1.turningLeft = false
    player1.turningRight = false
    player1.firing = false
    player1.primaryColor.r = 0.3
    player1.primaryColor.g = 0.5
    player1.primaryColor.b = 0.9
    player1.secondaryColor.r = 0.1
    player1.secondaryColor.g = 0.3
    player1.secondaryColor.b = 0.3
    player1.ticksUntilCanFire = 0
    player1.energy = SHIP_MAX_ENERGY
    player1.hit = false
    player1.dead = false

    player2.p.x = 600.0 * FIXED_POINT_SCALE_FACTOR
    player2.p.y = 400.0 * FIXED_POINT_SCALE_FACTOR
    player2.p.vx = 0.0
    player2.p.vy = 0.0
    player2.p.rotation = Random.nextInt(0, NUMBER_OF_ROTATION_ANGLES - 1)
    player2.p.radius = SHIP_RADIUS
    player2.thrusting = false
    player2.turningLeft = false
    player2.turningRight = false
    player2.firing = false
    player2.primaryColor.r = 0.9
    player2.primaryColor.g = 0.2
    player2.primaryColor.b = 0.3
    player2.secondaryColor.r = 0.5
    player2.secondaryColor.g = 0.2
    player2.secondaryColor.b = 0.3
    player2.ticksUntilCanFire = 0
    player2.energy = SHIP_MAX_ENERGY
    player2.hit = false
    player2.dead = false

    initStarsArray()
    initMissilesArray()

    gameOverMessage = null
}

/**
 * Handle key-pressed and key-released events
 */
fun onKeyEvent(keyval: Int, keyIsOn: Boolean) {
    when (keyval) {
        Gdk.KEY_Escape -> quit = true
        Gdk.KEY_bracketleft -> if (keyIsOn) debugScaleFactor /= 1.25f
        Gdk.KEY_bracketright -> if (keyIsOn) debugScaleFactor *= 1.25f
        Gdk.KEY_space -> if (gameOverMessage != null) reset()
        Gdk.KEY_a -> player1.turningLeft = keyIsOn
        Gdk.KEY_d -> player1.turningRight = keyIsOn
        Gdk.KEY_w -> player1.thrusting = keyIsOn
        Gdk.KEY_Control_L -> player1.firing = keyIsOn
        Gdk.KEY_Left -> player2.turningLeft = keyIsOn
        Gdk.KEY_KP_Left -> player2.turningLeft = keyIsOn
        Gdk.KEY_Right -> player2.turningRight = keyIsOn
        Gdk.KEY_KP_Right -> player2.turningRight = keyIsOn
        Gdk.KEY_Up -> player2.thrusting = keyIsOn
        Gdk.KEY_KP_Up -> player2.thrusting = keyIsOn
        Gdk.KEY_Control_R -> player2.firing = keyIsOn
        Gdk.KEY_KP_Insert -> player2.firing = keyIsOn
    }
}
