workspace "Structurizr Lite" "Structurizr Lite documentation" {

    model {
        lite = softwareSystem "Structurizr Lite" {
            !docs docs
        }
    }

    views {
        systemContext lite "SystemContext" {
            include *
            autoLayout
        }
    }

}