package net.voldrich.smscsim;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * SMSCsim command line options. Annotation driven.
 **/
@Parameters(commandDescription = "SMSCsim command line options")
public class ServerMainParameters {
	
	private List<String> smscPorts;
	
	private String logLevel = "DEBUG";

	public List<String> getSmscPorts() {
		return smscPorts;
	}

	@Parameter(names={"-p", "-port"}, description="List of SMSC ports", required=true, variableArity=true)
	public void setSmscPorts(List<String> smscPorts) {
		this.smscPorts = smscPorts;		
	}
	
	public List<Integer> getSmscPortsAsIntegers() {
		List<Integer> smscPortsInts = new ArrayList<Integer>(smscPorts.size());
		for (String str : smscPorts) {
			smscPortsInts.add(new Integer(str));
		}
		return smscPortsInts;
	}

	public String getLogLevel() {
		return logLevel;
	}

	@Parameter(names="-ll", description="Log level, one of: ALL, DEBUG, INFO, WARN, ERROR, FATAL, OFF, TRACE", required=false)
	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}
	

	private boolean help; 
	
	@Parameter(names = {"--help", "?", "help"}, description="Shows ussage", help = true)
	public void setHelp(boolean help) {
		this.help = help;
	}
	
	public boolean isHelp() {
		return help;
	}

}
