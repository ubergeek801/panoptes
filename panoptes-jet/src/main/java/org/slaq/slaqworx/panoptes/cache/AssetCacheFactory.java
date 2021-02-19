package org.slaq.slaqworx.panoptes.cache;

import com.hazelcast.jet.JetInstance;

public class AssetCacheFactory {
    public static AssetCache fromJetInstance(JetInstance jetInstance) {
        return new AssetCache(jetInstance.getHazelcastInstance());
    }
}
