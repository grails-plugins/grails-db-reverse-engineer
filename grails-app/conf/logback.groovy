import grails.util.BuildSettings
import grails.util.Environment

String defaultPattern = '%-65(%.-2level %date{HH:mm:ss.SSS} %logger{32}) - %message%n'

appender('STDOUT', ConsoleAppender) {
	encoder(PatternLayoutEncoder) {
		pattern = defaultPattern
	}
}

root ERROR, ['STDOUT']

File targetDir = BuildSettings.TARGET_DIR
if (Environment.developmentMode && targetDir) {

	appender('FULL_STACKTRACE', FileAppender) {
		file = "$targetDir/stacktrace.log"
		append = true
		encoder(PatternLayoutEncoder) {
			pattern = defaultPattern
		}
	}

	logger 'StackTrace', ERROR, ['FULL_STACKTRACE'], false
}
