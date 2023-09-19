#!/bin/sh
flatpak-builder --install --user --force-clean build/flatpak io.github.jwharm.javagi.examples.Browser.json
flatpak run io.github.jwharm.javagi.examples.Browser

