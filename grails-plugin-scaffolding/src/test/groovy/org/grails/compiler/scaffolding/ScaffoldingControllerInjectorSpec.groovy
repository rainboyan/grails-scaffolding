package org.grails.compiler.scaffolding

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import spock.lang.Specification

import grails.compiler.ast.ClassInjector
import grails.rest.RestfulController
import org.grails.compiler.injection.GrailsAwareClassLoader

class ScaffoldingControllerInjectorSpec extends Specification {

    def "Test Controller class was injected when has scaffold"() {
        given:
        def transformer = new ScaffoldingControllerInjector()
        def gcl = new GrailsAwareClassLoader(getClass().getClassLoader())
        gcl.disabledGlobalASTTransformations = true
        gcl.classInjectors = [transformer] as ClassInjector[]
        gcl.metaDataMap = [
                'GRAILS_APP_DIR': '/Users/grails/grails-demo-project/grails-app',
                'PROJECT_DIR': '/Users/grails/grails-demo-project',
                'PROJECT_TYPE': 'WEB_APP'
        ]

        when:
        def clazz = gcl.parseClass('''
@grails.artefact.Artefact('Controller')
class PostController {
    static scaffold = Post
}

@grails.persistence.Entity
class Post {
    String title
}
''', '/Users/grails/grails-demo-project/grails-app/src/main/groovy/org/demo/PostController.groovy')

        def classNode = gcl.getClassNode('PostController')

        then:
        classNode.superClass == ClassHelper.make(RestfulController)

    }

    def "Test Controller class was injected when has scaffold with true"() {
        given:
        def transformer = new ScaffoldingControllerInjector()
        def gcl = new GrailsAwareClassLoader(getClass().getClassLoader())
        gcl.disabledGlobalASTTransformations = true
        gcl.classInjectors = [transformer] as ClassInjector[]

        when:
        def clazz = gcl.parseClass('''
@grails.artefact.Artefact('Controller')
class PostController {
    static scaffold = true
}

''', '/Users/grails/grails-demo-project/grails-app/src/main/groovy/org/demo/PostController.groovy')

        def classNode = gcl.getClassNode('PostController')

        then:
        MultipleCompilationErrorsException e = thrown()
        e.message.contains "The 'scaffold' property must refer to a domain class."
    }

}
