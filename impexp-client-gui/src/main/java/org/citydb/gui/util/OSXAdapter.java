/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.gui.util;

import org.citydb.util.log.Logger;

import java.awt.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;

public class OSXAdapter implements InvocationHandler {
    public static final boolean IS_MAC_OS = System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("mac");
    private static Object macOSApplication;

    protected Object targetObject;
    protected Method targetMethod;
    protected String proxySignature;

    protected OSXAdapter(String proxySignature, Object target, Method handler) {
        this.proxySignature = proxySignature;
        this.targetObject = target;
        this.targetMethod = handler;
    }

    public static void setQuitHandler(Object target, Method quitHandler) {
        setHandler(new OSXAdapter("handleQuit", target, quitHandler));
    }

    public static void setAboutHandler(Object target, Method handler) {
        boolean enableAboutMenu = (target != null && handler != null);
        if (enableAboutMenu) {
            setHandler(new OSXAdapter("handleAbout", target, handler));
        }

        try {
            Method method = macOSApplication.getClass().getDeclaredMethod("setEnabledAboutMenu", boolean.class);
            method.invoke(macOSApplication, enableAboutMenu);
        } catch (Exception e) {
            Logger.getInstance().debug("Failed to activate the about entry in the macOS application menu.", e);
        }
    }

    public static void setPreferencesHandler(Object target, Method handler) {
        boolean enablePrefsMenu = (target != null && handler != null);
        if (enablePrefsMenu) {
            setHandler(new OSXAdapter("handlePreferences", target, handler));
        }

        try {
            Method method = macOSApplication.getClass().getDeclaredMethod("setEnabledPreferencesMenu", boolean.class);
            method.invoke(macOSApplication, enablePrefsMenu);
        } catch (Exception e) {
            Logger.getInstance().debug("Failed to enable the preferences entry in the macOS application menu.", e);
        }
    }

    public static void setFileHandler(Object target, Method handler) {
        setHandler(new OSXAdapter("handleOpenFile", target, handler) {
            public boolean callTarget(Object appleEvent) {
                if (appleEvent != null) {
                    try {
                        Method getFilenameMethod = appleEvent.getClass().getDeclaredMethod("getFilename", (Class<?>[]) null);
                        String filename = (String) getFilenameMethod.invoke(appleEvent, (Object[]) null);
                        this.targetMethod.invoke(this.targetObject, filename);
                    } catch (Exception e) {
                        //
                    }
                }
                return true;
            }
        });
    }

    public static void setDockIconImage(Image image) {
        try {
            Class<?> type = Class.forName("com.apple.eawt.Application");
            if (macOSApplication == null) {
                macOSApplication = type.getConstructor((Class<?>[]) null).newInstance((Object[]) null);
            }

            Method method = type.getDeclaredMethod("setDockIconImage", Image.class);
            method.invoke(macOSApplication, image);
        } catch (ClassNotFoundException e) {
            Logger.getInstance().debug("This version of macOS does not support the Apple EAWT.", e);
        } catch (Exception e) {
            Logger.getInstance().debug("Failed to access macOS EAWT.", e);
        }
    }

    public static void setHandler(OSXAdapter adapter) {
        try {
            Class<?> type = Class.forName("com.apple.eawt.Application");
            if (macOSApplication == null) {
                macOSApplication = type.getConstructor((Class<?>[]) null).newInstance((Object[]) null);
            }

            Class<?> listener = Class.forName("com.apple.eawt.ApplicationListener");
            Method method = type.getDeclaredMethod("addApplicationListener", listener);
            Object proxy = Proxy.newProxyInstance(OSXAdapter.class.getClassLoader(), new Class[]{listener}, adapter);
            method.invoke(macOSApplication, proxy);
        } catch (ClassNotFoundException e) {
            Logger.getInstance().debug("This version of macOS does not support the Apple EAWT.", e);
        } catch (Exception e) {
            Logger.getInstance().debug("Failed to access macOS EAWT.", e);
        }
    }

    public boolean callTarget(Object appleEvent) throws InvocationTargetException, IllegalAccessException {
        Object result = targetMethod.invoke(targetObject, (Object[]) null);
        return result == null || Boolean.parseBoolean(result.toString());
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isCorrectMethod(method, args)) {
            boolean handled = callTarget(args[0]);
            setApplicationEventHandled(args[0], handled);
        }

        return null;
    }

    protected boolean isCorrectMethod(Method method, Object[] args) {
        return (targetMethod != null
                && proxySignature.equals(method.getName())
                && args.length == 1);
    }

    protected void setApplicationEventHandled(Object event, boolean handled) {
        if (event != null) {
            try {
                Method setHandledMethod = event.getClass().getDeclaredMethod("setHandled", boolean.class);
                setHandledMethod.invoke(event, handled);
            } catch (Exception e) {
                Logger.getInstance().debug("Failed to handle a macOS EAWT application event.", e);
            }
        }
    }
}
