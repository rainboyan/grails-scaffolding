/*
 * Copyright 2022-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.generators

import org.grails.build.parsing.CommandLine
import org.grails.config.CodeGenConfig

/**
 * @author Michael Yan
 * @since 5.5.0
 */
class ScaffoldGenerator extends AbstractGenerator {

    private static final Map<String, String> TYPES = [
            'string': 'String',
            'String': 'String',
            'int': 'int',
            'integer': 'Integer',
            'Integer': 'Integer',
            'long': 'long',
            'Long': 'Long',
            'double': 'double',
            'Double': 'Double',
            'float': 'float',
            'Float': 'Float',
            'date': 'Date',
            'Date': 'Date',
            'boolean': 'boolean',
            'Boolean': 'Boolean'
    ]

    @Override
    boolean generate(CommandLine commandLine) {
        String[] args = commandLine.remainingArgs.toArray(new String[0])
        if (args.size() < 2) {
            return
        }
        boolean overwrite = commandLine.hasOption('force') || commandLine.hasOption('f')
        CodeGenConfig config = loadApplicationConfig()
        String className = args[1].capitalize()
        String propertyName = className.uncapitalize()
        String defaultPackage = config.getProperty('grails.codegen.defaultPackage')
        String packagePath = defaultPackage.replace('.', '/')
        Map<String, String> classAttributes = new LinkedHashMap<>()
        String[] attributes = (args.size() >= 3 ? args[2..-1] : []) as String[]
        attributes.each { String item ->
            String[] attr = (item.contains(':') ? item.split(':') : [item, 'String']) as String[]
            classAttributes[attr[0]] = TYPES[attr[1]] ?: attr[1]
        }

        Map<String, Object> model = new HashMap<>()
        model['packageName'] = defaultPackage
        model['className'] = className
        model['modelName'] = propertyName
        model['propertyName'] = propertyName
        model['classAttributes'] = classAttributes

        String domainClassFile = 'app/domain/' + packagePath + '/' + className + '.groovy'
        String domainClassSpecFile = 'src/test/groovy/' + packagePath + '/' + className + 'Spec.groovy'
        String controllerFile = 'app/controllers/' + packagePath + '/' + className + 'Controller.groovy'
        String controllerSpecFile = 'src/test/groovy/' + packagePath + '/' + className + 'ControllerSpec.groovy'
        String serviceFile = 'app/services/' + packagePath + '/' + className + 'Service.groovy'
        String serviceSpecFile = 'src/test/groovy/' + packagePath + '/' + className + 'ServiceSpec.groovy'
        String createGspFile = 'app/views/' + propertyName + '/' + 'create.gsp'
        String editGspFile = 'app/views/' + propertyName + '/' + 'edit.gsp'
        String indexGspFile = 'app/views/' + propertyName + '/' + 'index.gsp'
        String showGspFile = 'app/views/' + propertyName + '/' + 'show.gsp'

        createFile('scaffolding/DomainClass.groovy.tpl', domainClassFile, model, overwrite)
        createFile('scaffolding/Controller.groovy.tpl', controllerFile, model, overwrite)
        createFile('scaffolding/Service.groovy.tpl', serviceFile, model, overwrite)
        createFile('scaffolding/create.gsp.tpl', createGspFile, model, overwrite)
        createFile('scaffolding/edit.gsp.tpl', editGspFile, model, overwrite)
        createFile('scaffolding/index.gsp.tpl', indexGspFile, model, overwrite)
        createFile('scaffolding/show.gsp.tpl', showGspFile, model, overwrite)
        createFile('scaffolding/DomainClassSpec.groovy.tpl', domainClassSpecFile, model, overwrite)
        createFile('scaffolding/ControllerSpec.groovy.tpl', controllerSpecFile, model, overwrite)
        createFile('scaffolding/ServiceSpec.groovy.tpl', serviceSpecFile, model, overwrite)

        true
    }

}
