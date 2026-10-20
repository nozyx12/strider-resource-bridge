/*
 * Copyright (C) 2026 Nozyx
 *
 * This file is part of strider-resource-bridge.
 *
 * strider-resource-bridge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * strider-resource-bridge is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with strider-resource-bridge. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.nozyx.strider.resourcebridge;

import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;

import java.io.IOException;
import java.nio.file.Path;

class AddonResourceSupplier implements Pack.ResourcesSupplier {

    private final Path jarPath;
    private final String addonId;

    AddonResourceSupplier(Path jarPath, String addonId) {
        this.jarPath = jarPath;
        this.addonId = addonId;
    }

    @Override
    public PackResources openPrimary(PackLocationInfo location) {
        try {
            return new AddonPackResources(location, jarPath, addonId);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to open resources for addon pack " + location.id(),
                    e
            );
        }
    }

    @Override
    public PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
        try {
            return new AddonPackResources(location, jarPath, addonId);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to open resources for addon pack " + location.id(),
                    e
            );
        }
    }
}
