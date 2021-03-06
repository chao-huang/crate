/*
 * This file is part of a module with proprietary Enterprise Features.
 *
 * Licensed to Crate.io Inc. ("Crate.io") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 *
 * To use this file, Crate.io must have given you permission to enable and
 * use such Enterprise Features and you must have a valid Enterprise or
 * Subscription Agreement with Crate.io.  If you enable or use the Enterprise
 * Features, you represent and warrant that you have a valid Enterprise or
 * Subscription Agreement with Crate.io.  Your use of the Enterprise Features
 * if governed by the terms and conditions of your Enterprise or Subscription
 * Agreement with Crate.io.
 */

package io.crate.license;

import io.crate.expression.reference.sys.check.SysCheck;
import io.crate.expression.reference.sys.check.cluster.LicenseCheck;
import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.inject.multibindings.Multibinder;

public class LicenseModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<SysCheck> checksBinder = Multibinder.newSetBinder(binder(), SysCheck.class);
        checksBinder.addBinding().to(LicenseCheck.class);

        bind(LicenseService.class).to(EnterpriseLicenseService.class).asEagerSingleton();
        bind(TransportSetLicenseAction.class).asEagerSingleton();
    }
}
