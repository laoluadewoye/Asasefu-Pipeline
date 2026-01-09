module com.laoluade.ingestor.ao3 {
    requires com.google.common;
    requires jakarta.annotation;
    requires jakarta.persistence;
    requires static lombok;
    requires org.json;
    requires org.seleniumhq.selenium.api;
    requires org.seleniumhq.selenium.chrome_driver;
    requires org.seleniumhq.selenium.grid;
    requires org.seleniumhq.selenium.remote_driver;
    requires org.seleniumhq.selenium.support;
    requires org.slf4j;
    requires spring.aop;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.data.commons;
    requires spring.data.jpa;
    requires spring.messaging;
    requires spring.tx;
    requires spring.web;
    requires spring.websocket;

    exports com.laoluade.ingestor.ao3.core;
}
