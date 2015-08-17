package net.yazeed44.imagepicker.module;


import net.yazeed44.imagepicker.model.ImageEntry;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yazeed44 on 8/15/15.
 */
@Module
class DomainModule {

    @Provides
    @Singleton
    public ImageEntry provideTestString() {
        return new ImageEntry.Builder("").build();
    }
}
