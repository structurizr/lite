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
                        technology "Java Component"
                        matcher name-suffix "Component"
                        supportingTypes in-package
                        url prefix-src "https://github.com/structurizr/lite/blob/main/src/main/java"
                        forEach {
                            -> data "Reads from and writes to"
                            tag "Java Component"
                        }
                    }
                    strategy {
                        technology "Spring MVC Controller"
                        matcher annotation "org.springframework.stereotype.Controller"
                        filter exclude fqn-regex ".*AbstractController|.*.Http[0-9]*Controller"
                        url prefix-src "https://github.com/structurizr/lite/blob/main/src/main/java"
                        forEach {
                            -> ui "Renders HTML page in"
                        }
                    }
                    strategy {
                        technology "Spring MVC REST Controller"
                        matcher annotation "org.springframework.web.bind.annotation.RestController"
                        filter exclude fqn-regex ".*HealthCheckController"
                        description first-sentence
                        url prefix-src "https://github.com/structurizr/lite/blob/main/src/main/java"
                        forEach {
                            ui -> this "Makes API calls using"
                            tag "API Component"
                        }
                    }
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