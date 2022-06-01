package nextflow.cli

import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.Const
import nextflow.config.ConfigBuilder
import nextflow.exception.AbortOperationException
import nextflow.python.PythonLauncher

@Slf4j
@CompileStatic
@Parameters(commandDescription = "Execute a pipeline project defined in python")
class CmdPyRunBasic extends CmdRun {

    // This is redundant, using this CMD already marks python as interpreter
    @Parameter(names=['-py'], description = 'Execute the workflow using Python syntax')
    boolean usePython

    @Override
    String getName() { 'python' }

    @Override
    void run() {

        final scriptArgs = (args?.size()>1 ? args[1..-1] : []) as List<String>

        final pipeline = stdin ? '-' : ( args ? args[0] : null )
        if( !pipeline )
            throw new AbortOperationException("No project name was specified")

        //if( withPodman && withoutPodman )
        //    throw new AbortOperationException("Command line options `-with-podman` and `-without-podman` cannot be specified at the same time")

        //if( withDocker && withoutDocker )
        //    throw new AbortOperationException("Command line options `-with-docker` and `-without-docker` cannot be specified at the same time")

        //if( offline && latest )
        //    throw new AbortOperationException("Command line options `-latest` and `-offline` cannot be specified at the same time")

        if( dsl1 || dsl2 )
            throw new AbortOperationException("Command line options `-dsl1` and `-dsl2` cannot be specified at the same time as `-py`")

        // same rules as base CmdRun
        checkRunName() // TODO: maybe override to check if last is Py compatible

        // -- specify the arguments, ScriptFile is valid for Py too
        final scriptFile = getScriptFile(pipeline)

        // create the config object
        final builder = new ConfigBuilder()
                // this loads cli options
                .setOptions(launcher.options)
                .setCmdRun(this)
                .setBaseDir(scriptFile.parent)
        final config = builder .build()

        def driver = new PythonLauncher(
            pipeline: pipeline,
            scriptFile: scriptFile,
            config: config,
            args: scriptArgs
        )

        log.info '\n'
        log.info "N E X T F L O W  ~ Python experimental version"
        log.info '\n'

        driver.showInfo()
        driver.run()

        // TODO: maybe driver.shutdown()

        System.exit(0) // or any other status


    }
}
