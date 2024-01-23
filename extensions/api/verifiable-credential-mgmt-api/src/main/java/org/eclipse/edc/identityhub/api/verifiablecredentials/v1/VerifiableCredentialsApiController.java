/*
 *  Copyright (c) 2024 Metaform Systems, Inc.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Metaform Systems, Inc. - initial API and implementation
 *
 */

package org.eclipse.edc.identityhub.api.verifiablecredentials.v1;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import org.eclipse.edc.identityhub.spi.store.CredentialStore;
import org.eclipse.edc.identityhub.spi.store.model.VerifiableCredentialResource;
import org.eclipse.edc.spi.query.Criterion;
import org.eclipse.edc.spi.query.QuerySpec;
import org.eclipse.edc.spi.result.ServiceResult;
import org.eclipse.edc.web.spi.exception.InvalidRequestException;
import org.eclipse.edc.web.spi.exception.ObjectNotFoundException;

import java.util.Collection;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.edc.web.spi.exception.ServiceResultHandler.exceptionMapper;

@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Path("/v1/credentials")
public class VerifiableCredentialsApiController implements VerifiableCredentialsApi {

    private final CredentialStore credentialStore;

    public VerifiableCredentialsApiController(CredentialStore credentialStore) {
        this.credentialStore = credentialStore;
    }

    @GET
    @Path("/{credentialId}")
    @Override
    public VerifiableCredentialResource findById(@PathParam("credentialId") String id) {
        var result = credentialStore.query(QuerySpec.Builder.newInstance().filter(new Criterion("id", "=", id)).build())
                .orElseThrow(InvalidRequestException::new);
        return result.findFirst().orElseThrow(() -> new ObjectNotFoundException(VerifiableCredentialResource.class, id));
    }

    @GET
    @Override
    public Collection<VerifiableCredentialResource> findByType(@QueryParam("type") String type) {
        var query = QuerySpec.Builder.newInstance()
                .filter(new Criterion("verifiableCredential.credential.types", "contains", type))
                .build();

        return credentialStore.query(query).orElseThrow(InvalidRequestException::new).toList();
    }

    @DELETE
    @Path("/{credentialId}")
    @Override
    public void deleteCredential(@PathParam("credentialId") String id) {
        var res = credentialStore.deleteById(id);
        if (res.failed()) {
            throw exceptionMapper(VerifiableCredentialResource.class, id).apply(ServiceResult.fromFailure(res).getFailure());
        }
    }
}