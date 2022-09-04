workspace {

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