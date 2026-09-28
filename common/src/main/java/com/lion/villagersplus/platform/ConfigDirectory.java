package com.lion.villagersplus.platform;


import java.nio.file.Path;

public class ConfigDirectory {

    public static Path getConfigDirectory() {
        return com.lion.villagersplus.platform.fabric.ConfigDirectoryImpl.getConfigDirectory();
    }
}
