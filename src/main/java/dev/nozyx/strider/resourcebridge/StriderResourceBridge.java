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

import dev.nozyx.strider.loader.api.Addon;
import dev.nozyx.strider.loader.api.IStriderLoader;
import dev.nozyx.strider.loader.api.TransformationUtils;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.flag.FeatureFlagSet;

import javax.swing.*;
import java.util.*;

public class StriderResourceBridge implements Addon {

    public static final List<Pack> packs = new ArrayList<>();

    @Override
    public void onInitialize(IStriderLoader loader) {
        loader.getAddons().forEach((id, addon) -> {
            PackLocationInfo locationInfo = new PackLocationInfo(
                    "striderloader.addon/" + id,
                    Component.literal(addon.info().name()),
                    PackSource.BUILT_IN,
                    Optional.empty()
            );

            Pack pack = new Pack(
                    locationInfo,
                    new AddonResourceSupplier(addon.jarPath(), id),
                    new Pack.Metadata(
                            Component.literal("[" + id + "]")
                                    .append(Component.literal("\n  > Addon resources")
                                            .withStyle(ChatFormatting.DARK_AQUA)),
                            PackCompatibility.COMPATIBLE,
                            FeatureFlagSet.of(),
                            List.of()
                    ),
                    new PackSelectionConfig(true, Pack.Position.TOP, false)
            );

            packs.add(pack);
        });

        TransformationUtils.transformClass(
                loader.getClassLoader(),
                "net.minecraft.server.packs.repository.PackRepository",
                builder -> builder
                        .visit(
                                Advice.to(PackAdvice.class)
                                        .on(ElementMatchers.isConstructor())
                        )
        );
    }

    @Override
    public void onReady(IStriderLoader loader) {
    }

    public static class PackAdvice {

        @Advice.OnMethodEnter
        public static void onEnter(
                @Advice.Argument(value = 0, readOnly = false)
                RepositorySource[] sources
        ) {
            RepositorySource[] newSources =
                    Arrays.copyOf(sources, sources.length + 1);

            newSources[sources.length] = packs::forEach;
            sources = newSources;
        }
    }
}
