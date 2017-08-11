package io.github.iwag.jerseystarter;

import io.github.iwag.jerseystarter.resources.TaskResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import javax.ws.rs.core.Application;

public class TaskTest extends JerseyTest {
    @Override
    protected Application configure() {
        return new ResourceConfig(TaskResource.class);
    }
}