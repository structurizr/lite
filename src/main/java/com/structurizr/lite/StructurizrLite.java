package com.structurizr.lite;

import com.structurizr.Workspace;
import com.structurizr.api.WorkspaceApiClient;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.encryption.AesEncryptionStrategy;
import com.structurizr.autolayout.graphviz.GraphvizAutomaticLayout;
import com.structurizr.importer.documentation.DefaultDocumentationImporter;
import com.structurizr.lite.util.DateUtils;
import com.structurizr.lite.util.Version;
import com.structurizr.util.StringUtils;
import com.structurizr.util.WorkspaceUtils;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.Filter;
import org.apache.catalina.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.descriptor.web.JspConfigDescriptorImpl;
import org.apache.tomcat.util.descriptor.web.JspPropertyGroup;
import org.apache.tomcat.util.descriptor.web.JspPropertyGroupDescriptorImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.io.File;
import java.util.Collections;

@SpringBootApplication(exclude= {UserDetailsServiceAutoConfiguration.class})
@EnableScheduling
public class StructurizrLite extends SpringBootServletInitializer {

	private static final String DEFAULT_STRUCTURIZR_DATA_DIRECTORY = "/usr/local/structurizr";
	private static final String STRUCTURIZR_USERNAME = "STRUCTURIZR_USERNAME";

	public static void main(String[] args) {
		File structurizrDataDirectory = new File(DEFAULT_STRUCTURIZR_DATA_DIRECTORY);
		if (args.length > 0) {
			structurizrDataDirectory = new File(args[0]);
		}

		Configuration.getInstance().setDataDirectory(structurizrDataDirectory);

		SpringApplication.run(StructurizrLite.class, args);
		start();
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(StructurizrLite.class);
	}

	@Bean
	public FilterRegistrationBean<? extends Filter> filterRegistrationBean() {
		CharacterEncodingFilter filter = new CharacterEncodingFilter();
		filter.setEncoding("UTF-8");
		filter.setForceEncoding(true);

		FilterRegistrationBean<CharacterEncodingFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(filter);
		registrationBean.addUrlPatterns("/*");

		return registrationBean;
	}

	@Bean
	public ConfigurableServletWebServerFactory configurableServletWebServerFactory ( ) {
		return new TomcatServletWebServerFactory() {
			@Override
			protected void postProcessContext(Context context) {
				super.postProcessContext(context);
				JspPropertyGroup jspPropertyGroup = new JspPropertyGroup();
				jspPropertyGroup.addUrlPattern("*.jsp");
				jspPropertyGroup.setPageEncoding("UTF-8");
				jspPropertyGroup.setScriptingInvalid("true");
				jspPropertyGroup.addIncludePrelude("/WEB-INF/fragments/prelude.jspf");
				jspPropertyGroup.addIncludeCoda("/WEB-INF/fragments/coda.jspf");
				jspPropertyGroup.setTrimWhitespace("true");
				jspPropertyGroup.setDefaultContentType("text/html");

				JspPropertyGroupDescriptorImpl jspPropertyGroupDescriptor = new JspPropertyGroupDescriptorImpl(jspPropertyGroup);
				context.setJspConfigDescriptor(new JspConfigDescriptorImpl(Collections.singletonList(jspPropertyGroupDescriptor), Collections.emptyList()));
			}
		};
	}

	private static void start() {
		Log log = LogFactory.getLog(StructurizrLite.class);

		log.info("***********************************************************************************");
		log.info("  _____ _                   _              _          ");
		log.info(" / ____| |                 | |            (_)         ");
		log.info("| (___ | |_ _ __ _   _  ___| |_ _   _ _ __ _ _____ __ ");
		log.info(" \\___ \\| __| '__| | | |/ __| __| | | | '__| |_  / '__|");
		log.info(" ____) | |_| |  | |_| | (__| |_| |_| | |  | |/ /| |   ");
		log.info("|_____/ \\__|_|   \\__,_|\\___|\\__|\\__,_|_|  |_/___|_|   ");
		log.info("                                                      ");
		log.info("Structurizr Lite");
		log.info(" - build: " + new Version().getBuildNumber() + " (" + DateUtils.formatIsoDate(new Version().getBuildTimestamp()) + ")");

		try {
			log.info(" - structurizr-java: v" + Class.forName(Workspace.class.getCanonicalName()).getPackage().getImplementationVersion());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			log.info(" - structurizr-dsl: v" + Class.forName(StructurizrDslParser.class.getCanonicalName()).getPackage().getImplementationVersion());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			log.info(" - structurizr-import: v" + Class.forName(DefaultDocumentationImporter.class.getCanonicalName()).getPackage().getImplementationVersion());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			log.info(" - structurizr-graphviz: v" + Class.forName(GraphvizAutomaticLayout.class.getCanonicalName()).getPackage().getImplementationVersion());
		} catch (Exception e) {
			e.printStackTrace();
		}

		log.info("");
		log.info("Workspace path: " + Configuration.getInstance().getDataDirectory().getAbsolutePath());
		log.info("Workspace filename: " + Configuration.getInstance().getWorkspaceFilename() + "[.dsl|.json]");
		log.info("Workspaces: " + (Configuration.getInstance().isSingleWorkspace() ? "1" : "*"));
		log.info("URL: " + Configuration.getInstance().getWebUrl());
		log.info("Auto-save interval: " + Configuration.getInstance().getAutoSaveInterval() + "ms");
		log.info("Auto-refresh interval: " + Configuration.getInstance().getAutoRefreshInterval() + "ms");

		try {
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.command("dot", "-V");
			Process process = processBuilder.start();
			int exitCode = process.waitFor();

			String input = new String(process.getInputStream().readAllBytes());
			String error = new String(process.getErrorStream().readAllBytes());
			Configuration.getInstance().setGraphvizEnabled(exitCode == 0);

			log.debug("Running: dot -V");
			log.debug("stdout: " + input);
			log.debug("stderr: " + error);
		} catch (Exception e) {
			log.warn(e.getMessage());
		}
		log.info("Graphviz (dot): " + Configuration.getInstance().isGraphvizEnabled());

		if (Configuration.getInstance().isSingleWorkspace()) {
			try {
				long workspaceId = Configuration.getInstance().getRemoteWorkspaceId();
				if (workspaceId > 0) {
					log.info("");

					String branch = "";
					if (!StringUtils.isNullOrEmpty(Configuration.getInstance().getRemoteBranch())) {
						branch = " (branch=" + Configuration.getInstance().getRemoteBranch() + ")";
					}
					log.info("Pulling workspace from " + Configuration.getInstance().getRemoteApiUrl() + branch + " with ID " + workspaceId);

					Workspace workspace = createWorkspaceApiClient().getWorkspace(workspaceId);

					File jsonFile = new File(Configuration.getInstance().getDataDirectory(), Configuration.getInstance().getWorkspaceFilename() + ".json");
					if (workspace.getLastModifiedDate().getTime() > jsonFile.lastModified()) {
						WorkspaceUtils.saveWorkspaceToJson(workspace, jsonFile);
					} else {
						log.info("Skipping - local " + Configuration.getInstance().getWorkspaceFilename() + ".json file is newer");
					}
				}
			} catch (Exception e) {
				log.error(e);
			}
		}
		
		log.info("***********************************************************************************");
		log.info("MIT License");
		log.info("");
		log.info("Copyright (c) 2024 Structurizr Limited");
		log.info("");
		log.info("Permission is hereby granted, free of charge, to any person obtaining a copy");
		log.info("of this software and associated documentation files (the \"Software\"), to deal");
		log.info("in the Software without restriction, including without limitation the rights");
		log.info("to use, copy, modify, merge, publish, distribute, sublicense, and/or sell");
		log.info("copies of the Software, and to permit persons to whom the Software is");
		log.info("furnished to do so, subject to the following conditions:");
		log.info("");
		log.info("The above copyright notice and this permission notice shall be included in all");
		log.info("copies or substantial portions of the Software.");
		log.info("");
		log.info("THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR");
		log.info("IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,");
		log.info("FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE");
		log.info("AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER");
		log.info("LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,");
		log.info("OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE");
		log.info("SOFTWARE.");
		log.info("***********************************************************************************");
	}

	@PreDestroy
	public void stop() {
		Log log = LogFactory.getLog(StructurizrLite.class);

		log.info("********************************************************");
		log.info(" Stopping Structurizr Lite");

		if (Configuration.getInstance().isSingleWorkspace()) {
			try {
				long workspaceId = Configuration.getInstance().getRemoteWorkspaceId();
				if (workspaceId > 0) {
					log.info("");

					String branch = "";
					if (!StringUtils.isNullOrEmpty(Configuration.getInstance().getRemoteBranch())) {
						branch = " (branch=" + Configuration.getInstance().getRemoteBranch() + ")";
					}
					log.info("Pushing workspace to " + Configuration.getInstance().getRemoteApiUrl() + branch + " with ID " + workspaceId);

					Workspace workspace = WorkspaceUtils.loadWorkspaceFromJson(new File(Configuration.getInstance().getDataDirectory(), Configuration.getInstance().getWorkspaceFilename() + ".json"));
					createWorkspaceApiClient().putWorkspace(workspaceId, workspace);
				}
			} catch (Exception e) {
				log.error(e);
			}
		}

		log.info("********************************************************");
	}


	private static WorkspaceApiClient createWorkspaceApiClient() {
		String apiUrl = Configuration.getInstance().getRemoteApiUrl();
		String apiKey = Configuration.getInstance().getRemoteApiKey();
		String apiSecret = Configuration.getInstance().getRemoteApiSecret();
		String passphrase = Configuration.getInstance().getRemotePassphrase();
		String branch = Configuration.getInstance().getRemoteBranch();

		WorkspaceApiClient client = new WorkspaceApiClient(apiUrl, apiKey, apiSecret);
		client.setAgent("structurizr-lite/" + new Version().getBuildNumber());
		client.setUser(System.getenv(STRUCTURIZR_USERNAME));
		client.setBranch(branch);
		if (!StringUtils.isNullOrEmpty(passphrase)) {
			client.setEncryptionStrategy(new AesEncryptionStrategy(passphrase));
		}
		client.setMergeFromRemote(false);

		return client;
	}

}