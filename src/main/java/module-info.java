module pe.puyu.sweetprinterpos {
	requires transitive javafx.graphics;
	requires javafx.controls;
	requires javafx.fxml;

	requires java.desktop;

	requires ch.qos.logback.classic;
	requires ch.qos.logback.core;
	requires org.slf4j;

	requires com.google.gson;
	requires org.hildan.fxgson;
	opens pe.puyu.sweetprinterpos.model to com.google.gson, org.hildan.fxgson;
	opens pe.puyu.sweetprinterpos.services.api to com.google.gson, org.hildan.fxgson;

	requires net.harawata.appdirs;

	requires escpos.coffee;
	requires com.fazecast.jSerialComm;
	requires pe.puyu.jticketdesing;
	requires org.jetbrains.annotations;

	requires io.javalin;
	requires java.net.http;
	requires tyrus.standalone.client;
	requires ormlite.jdbc;
	requires java.sql;
	requires com.h2database;
	opens pe.puyu.sweetprinterpos.repository.model to ormlite.jdbc;

	opens pe.puyu.sweetprinterpos.views to javafx.fxml, javafx.graphics;
	opens pe.puyu.sweetprinterpos.app to javafx.fxml, javafx.graphics;

	exports pe.puyu.sweetprinterpos.app;
	exports pe.puyu.sweetprinterpos.repository.model;
	exports pe.puyu.sweetprinterpos.util;
}