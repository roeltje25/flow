package com.vaadin.flow.plugin.hotswapagent;

import javax.servlet.ServletException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.internal.BrowserLiveReload;
import com.vaadin.flow.internal.BrowserLiveReloadAccess;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.server.startup.ApplicationRouteRegistry;

/**
 * This is a unit test.
 */
public class FlowIntegrationTest {

    private FlowIntegration flowIntegration;

    private VaadinContext mockVaadinContext;

    private BrowserLiveReload browserLiveReloadMock;

    @Before
    public void setup() throws ServletException {
        VaadinServlet vaadinServletMock = Mockito.mock(VaadinServlet.class);
        VaadinServletService vaadinServiceMock = Mockito.mock(VaadinServletService.class);
        mockVaadinContext = new MockVaadinContext();
        Instantiator instantiatorMock = Mockito.mock(Instantiator.class);
        BrowserLiveReloadAccess browserLiveReloadAccesMock = Mockito
                .mock(BrowserLiveReloadAccess.class);
        browserLiveReloadMock = Mockito.mock(BrowserLiveReload.class);

        Mockito.when(vaadinServletMock.getService())
                .thenReturn(vaadinServiceMock);

        Mockito.when(vaadinServiceMock.getContext())
                .thenReturn(mockVaadinContext);
        Mockito.when(vaadinServiceMock.getInstantiator())
                .thenReturn(instantiatorMock);

        Mockito.when(
                instantiatorMock.getOrCreate(BrowserLiveReloadAccess.class))
                .thenReturn(browserLiveReloadAccesMock);

        Mockito.when(browserLiveReloadAccesMock.getLiveReload(Mockito.any()))
                .thenReturn(browserLiveReloadMock);

        flowIntegration = new FlowIntegration();
        flowIntegration.servletInitialized(vaadinServletMock);
    }

    @Test
    public void newRouteAnnotatedClass_reload_browserReloadInvoked() {
        // when
        flowIntegration.reload(hashSet(), hashSet());

        // then
        Mockito.verify(browserLiveReloadMock).reload();
    }

    @Test
    public void newRouteAnnotatedClass_reload_routeIsAddedToRegistry() {
        // given
        @Route("a")
        class A extends Component {
        }

        // when
        flowIntegration.reload(hashSet(A.class), hashSet());

        // then
        ApplicationRouteRegistry registry = ApplicationRouteRegistry
                .getInstance(mockVaadinContext);
        Assert.assertTrue(registry.getConfiguration().hasRoute("a"));
    }

    @Test
    public void deletedRouteAnnotatedClass_reload_routeIsRemovedFromRegistry() {
        // given
        @Route("a")
        class A extends Component {
        }

        ApplicationRouteRegistry registry = ApplicationRouteRegistry
                .getInstance(mockVaadinContext);
        registry.setRoute("a", A.class, Collections.emptyList());
        Assert.assertTrue(registry.getConfiguration().hasRoute("a"));

        // when
        flowIntegration.reload(hashSet(), hashSet(A.class));

        // then
        Assert.assertFalse(registry.getConfiguration().hasRoute("a"));
    }

    @Test
    public void renamedRouteAnnotatedClass_reload_routeIsUpdatedInRegistry() {
        // given
        @Route("aa")
        class A extends Component {
        }
        ApplicationRouteRegistry registry = ApplicationRouteRegistry
                .getInstance(mockVaadinContext);
        registry.setRoute("a", A.class, Collections.emptyList());
        Assert.assertTrue(registry.getConfiguration().hasRoute("a"));

        // when
        flowIntegration.reload(hashSet(A.class), hashSet());

        // then
        Assert.assertFalse(registry.getConfiguration().hasRoute("a"));
        Assert.assertTrue(registry.getConfiguration().hasRoute("aa"));
    }

    private HashSet<Class<?>> hashSet(Class<?>... cls) {
        return Arrays.stream(cls)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static class MockVaadinContext implements VaadinContext {

        private Map<Class<?>, Object> attrs = new HashMap<>();

        @Override
        public <T> T getAttribute(Class<T> type,
                Supplier<T> defaultValueSupplier) {
            return (T) attrs.getOrDefault(type,
                    defaultValueSupplier == null ? null
                            : defaultValueSupplier.get());
        }

        @Override
        public <T> void setAttribute(Class<T> clazz, T value) {
            attrs.put(clazz, value);
        }

        @Override
        public void removeAttribute(Class<?> clazz) {
            attrs.remove(clazz);
        }

        @Override
        public Enumeration<String> getContextParameterNames() {
            return null;
        }

        @Override
        public String getContextParameter(String name) {
            return null;
        }
    }

}