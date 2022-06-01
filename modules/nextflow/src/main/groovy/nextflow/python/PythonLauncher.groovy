package nextflow.python

import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import nextflow.config.ConfigMap
import nextflow.script.ScriptFile
import org.graalvm.polyglot.*

import java.util.concurrent.Callable


@Slf4j
@TupleConstructor
class PythonLauncher {

    String pipeline
    ScriptFile scriptFile
    List<String> args
    ConfigMap config

    void showInfo() {

        log.info(center("Python Launcher configuration"))
        log.info("pipeline:${pipeline}")
        def sFile = [
                source: scriptFile.source,
                main: scriptFile.main,
                repository: scriptFile.repository,
                revisionInfo: scriptFile.revisionInfo,
                localPath: scriptFile.localPath,
                projectName: scriptFile.projectName
        ]
        log.info("scriptFile:${sFile}")
        log.info("config:${config}")
        log.info("args:${args}")
        log.info(separator())
    }

    private void evalPythonScript(final Context ctx) {
        def res = ctx.eval("python", scriptFile.text)
        if ( !res.null ) {
            log.info(res.toString())
        }
    }

    private void exposeJavaMethods(final Context ctx) {
        ctx.getBindings("python")
        .putMember("javaMethod", this::javaMethod as Callable<Void>)
    }

    private void javaMethod() {
        println("this is being executed inside a java method")
    }

    /**
     * Launch the pipeline execution
     */
    void run() {

        if (!scriptFile.text.isEmpty()) {
            try(Context ctx = Context.newBuilder().allowAllAccess(true).build()) {
                log.info(center("Python output"))

                exposeJavaMethods(ctx)
                evalPythonScript(ctx)

                log.info(separator())
            }
        } else {
            log.info("The provided script file is empty")
        }

        log.info("Done interpreting python code")
    }

    // TODO: add shutdown method (?)

    private String center(String banner, String c = '~', int width = 80) {
        banner = " " + banner + " "
        def padding = (width - banner.length()).intdiv(2)
        def pad = c * padding
        return "${pad}${banner}${pad}"
    }

    private String separator(String c = '~', int width = 80) {
        return c * width
    }
}
