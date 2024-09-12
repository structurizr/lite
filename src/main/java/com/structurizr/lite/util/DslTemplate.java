package com.structurizr.lite.util;

import java.util.Random;

public class DslTemplate {

    private static final String DSL_TEMPLATE = """
workspace {

    !identifiers hierarchical

    model {
        u = person "User"
        ss = softwareSystem "Software System" {
            wa = container "Web Application"
            db = container "Database Schema" {
                tags "Database"
            }
            
            u -> wa "Uses"
            wa -> db "Reads from"
        }
    }
    
    views {
        systemContext ss "Diagram1" {
            include *
            autolayout lr
        }
        
        container ss "Diagram2" {
            include *
            autolayout lr
        }
        
        styles {
            element "Element" {
                color %s
            }
            element "Person" {
                background %s
                shape person
            }
            element "Software System" {
                background %s
            }
            element "Container" {
                background %s
            }
                element "Database" {
                shape cylinder
            }
        }
    }

    configuration {
        scope softwaresystem
    }

}""";

    private final static String[][] COLOURS = {
            { "#ffffff", "#05527d", "#066296", "#0773af" }, // blue
            { "#ffffff", "#048c04", "#047804", "#55aa55" }, // dark green
            { "#ffffff", "#199b65", "#1eba79", "#23d98d" }, // light green
            { "#ffffff", "#9b191f", "#ba1e25", "#d9232b" }, // red
            { "#ffffff", "#d34407", "#f86628", "#f88728" }, // orange
            { "#ffffff", "#ba1e75", "#d92389", "#f8289c" }, // pink
            { "#ffffff", "#741eba", "#8723d9", "#9a28f8" }, // purple
    };

    public static String generate() {
        int randomInt = new Random().nextInt(COLOURS.length);
        String[] colours = COLOURS[randomInt];
        return String.format(DSL_TEMPLATE, colours[0], colours[1], colours[2], colours[3]);
    }

}