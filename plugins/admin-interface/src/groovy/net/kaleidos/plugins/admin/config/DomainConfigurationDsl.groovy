package net.kaleidos.plugins.admin.config

class DomainConfigurationDsl {
    Class clazz
    Closure closure
    Map params = [:]

    public DomainConfigurationDsl(Class clazz, Closure closure) {
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure.delegate = this
        this.closure = closure
        this.clazz = clazz
    }

    public DomainConfig execute() {
        this.params = [:]
        this.closure()
        return _buildDomainConfig(this.params)
    }

    private _buildDomainConfig(Map params) {
        def domainConfig = new DomainConfig(clazz)
        params.each { method, properties ->
            if (['list', 'create', 'edit'].contains(method)) {
                if (properties['excludes'] && properties['includes']) {
                    throw new RuntimeException("The includes and exludes configuration is setted for domain: ${domainClass.name}. Only one can be defined")
                }

                if (properties['excludes']) {
                    domainConfig.excludes[method] = properties['excludes']
                }

                if (properties['includes']) {
                    domainConfig.includes[method] = properties['includes']
                }

                if (properties['customWidgets']) {
                    domainConfig.customWidgets[method] = properties['customWidgets']
                }

                if (properties['groups']) {
                    domainConfig.fieldGroups = properties['groups']
                }
            }
        }
        return domainConfig
    }

    def widget(Map attributes=[:], String property, String clazz) {
        ['create','edit'].each{
            this.params[it] = this.params[it] ?: [:]
            this.params[it]['customWidgets'] = this.params[it]['customWidgets'] ?: [:]
            this.params[it]['customWidgets'][property] = ["class":clazz, attributes:attributes]
        }
    }


    def widget(Map attributes=[:], String property) {
        return widget(attributes, property, null)
    }

    def groups(Closure cls) {
        def groupDsl = new DomainConfigGroupDsl()
        cls.resolveStrategy = Closure.DELEGATE_ONLY
        cls.delegate = groupDsl
        cls()

        ['create','edit'].each{
            this.params[it] = this.params[it] ?: [:]
            this.params[it]['groups'] = groupDsl.groups
        }
    }

    def methodMissing(String name, args) {
        assert [ 'list', 'create', 'edit' ].contains(name), "$name is not a valid property"
        if (['list', 'create', 'edit'].contains(name)) {
            assert args.size() == 1, "$args is not valid"
            assert args[0] instanceof Map, "${ args[0] } is not valid"

            this.params[name] = args[0]
        }
    }


}

class DomainConfigGroupDsl {
    Map groups = [:]

    def methodMissing(String name, args) {
        groups[name] = args.first()
    }
}

