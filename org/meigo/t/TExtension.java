package org.meigo.t;

import org.meigo.t.classes.*;
import php.runtime.env.CompileScope;
import php.runtime.ext.support.Extension;

public class TExtension extends Extension
{
    public static final String NS = "system";

    public TExtension() {}

    public Extension.Status getStatus()
    {
        return Extension.Status.EXPERIMENTAL;
    }

    public String[] getPackageNames()
    {
        return new String[] { "system" };
    }

    public void onRegister(CompileScope scope)
    {
        registerClass(scope, TulparServer.class);
    }
}
