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

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class AddonPackResources implements PackResources {

    private final PackLocationInfo location;
    private final FileSystem fileSystem;
    private final String addonId;

    public AddonPackResources(
            PackLocationInfo location,
            Path addonJar,
            String addonId
    ) throws IOException {
        this.location = location;
        this.addonId = addonId;

        this.fileSystem = FileSystems.newFileSystem(
                addonJar,
                (ClassLoader) null
        );
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... path) {
        if (path.length == 1 && path[0].equals("pack.png")) {
            Path icon = fileSystem.getPath(
                    "/",
                    "assets",
                    addonId,
                    "icon.png"
            );

            if (!Files.isRegularFile(icon)) {
                return null;
            }

            return () -> Files.newInputStream(icon);
        }

        Path resource = fileSystem.getPath("/", path);

        if (!Files.isRegularFile(resource)) {
            return null;
        }

        return () -> Files.newInputStream(resource);
    }

    @Override
    public IoSupplier<InputStream> getResource(
            PackType type,
            Identifier location
    ) {
        Path resource = fileSystem.getPath(
                "/",
                type.getDirectory(),
                location.getNamespace(),
                location.getPath()
        );

        if (!Files.isRegularFile(resource)) {
            return null;
        }

        return () -> Files.newInputStream(resource);
    }

    @Override
    public void listResources(
            PackType type,
            String namespace,
            String directory,
            ResourceOutput output
    ) {
        Path root = fileSystem.getPath(
                "/",
                type.getDirectory(),
                namespace,
                directory
        );

        if (!Files.isDirectory(root)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(root)) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        Path relative = root.relativize(path);

                        String resourcePath = directory.isEmpty()
                                ? relative.toString()
                                : directory + "/" + relative;

                        resourcePath = resourcePath.replace(
                                File.separatorChar,
                                '/'
                        );

                        output.accept(
                                Identifier.fromNamespaceAndPath(
                                        namespace,
                                        resourcePath
                                ),
                                () -> Files.newInputStream(path)
                        );
                    });
        } catch (IOException ignored) {
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        Path root = fileSystem.getPath(
                "/",
                type.getDirectory()
        );

        if (!Files.isDirectory(root)) {
            return Set.of();
        }

        Set<String> namespaces = new HashSet<>();

        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(root)) {

            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    namespaces.add(
                            path.getFileName().toString()
                    );
                }
            }
        } catch (IOException ignored) {
        }

        return namespaces;
    }

    @Override
    public <T> T getMetadataSection(
            MetadataSectionType<T> metadataSerializer
    ) {
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return location;
    }

    @Override
    public void close() {
        try {
            fileSystem.close();
        } catch (IOException ignored) {
        }
    }
}
