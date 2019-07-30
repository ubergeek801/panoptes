package org.slaq.slaqworx.panoptes.data;

import org.springframework.data.repository.CrudRepository;

import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

public interface SecurityAttributeRepository extends CrudRepository<SecurityAttribute<?>, String> {
    // trivial extension
}
