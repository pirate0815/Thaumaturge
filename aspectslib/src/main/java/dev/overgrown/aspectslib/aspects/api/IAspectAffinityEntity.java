package dev.overgrown.aspectslib.aspects.api;

import dev.overgrown.aspectslib.aspects.data.AspectData;

public interface IAspectAffinityEntity {
    AspectData aspectslib$getOriginalAspectData();
    void aspectslib$setOriginalAspectData(AspectData data);

    AspectData aspectslib$getAspectData();
    void aspectslib$setAspectData(AspectData data);
}