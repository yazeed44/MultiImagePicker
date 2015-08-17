package net.yazeed44.imagepicker.module;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by yazeed44 on 8/15/15.
 */
@Singleton
@Component(modules = {AppModule.class})
public interface AppContextComponent {

    Context provideApplicationContext();
}
