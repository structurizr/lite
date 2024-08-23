workspace "Structurizr Lite" {

    !identifiers hierarchical
    !const STRUCTURIZR_LITE_HOME "/Users/simon/sandbox/structurizr-lite"

    model {
        structurizrLite = softwareSystem "Structurizr Lite" {
            ui = container "structurizr-ui" {
                tags "Browser App"
            }
            data = container "structurizr-data" {
                tags "File System"
            }

            server = container "structurizr-lite" {
                technology "Java and Spring"

                !components {
                    classes "${STRUCTURIZR_LITE_HOME}/build/libs/structurizr-lite-plain.jar"
                    source "${STRUCTURIZR_LITE_HOME}/src/main/java"
                    strategy {
                        matcher nameSuffix "Component" "Java Component"
                        supportingTypes inpackage
                    }
                    strategy {
                        matcher annotation "org.springframework.stereotype.Controller" "Spring MVC Controller"
                        filter excludeRegex ".*AbstractController|.*.Http[0-9]*Controller"
                    }
                    strategy {
                        matcher annotation "org.springframework.web.bind.annotation.RestController" "Spring MVC REST Controller"
                        filter excludeRegex ".*HealthCheckController"
                    }
                }

                !script groovy {
                    element.components.each { it.url = it.properties["component.src"].replace(context.getDslParser().getConstant("STRUCTURIZR_LITE_HOME") + "/src/main/java", "https://github.com/structurizr/lite/blob/main/src/main/java") }
                }

                !elements "element.parent==server && element.technology==Java Component" {
                    -> data "Reads from and writes to"
                    tag "Java Component"
                }

                !elements "element.parent==server && element.technology==Spring MVC REST Controller" {
                    ui -> this "Makes API calls using"
                    tag "API Component"
                }

                !elements "element.parent==server && element.technology==Spring MVC Controller" {
                    -> ui "Renders HTML page in"
                }
            }
        }
    }

    views {
        container structurizrLite "Containers" {
            include *
        }

        component structurizrLite.server "Components" {
            include *
        }

        styles {
            element "Browser App" {
                shape webbrowser
            }

            element "File System" {
                shape folder
            }

            element "API Component" {
                shape hexagon
            }
        }
    }

}