package org.hisp.dhis.audit;

/*
 * Copyright (c) 2004-2019, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Repository
public class JdbcAuditRepository implements AuditRepository
{
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert auditInsert;

    public JdbcAuditRepository( JdbcTemplate jdbcTemplate )
    {
        this.jdbcTemplate = jdbcTemplate;
        this.auditInsert = new SimpleJdbcInsert( jdbcTemplate )
            .withTableName( "audit" )
            .usingGeneratedKeyColumns( "auditid" );
    }

    @Override
    @Transactional
    public long save( Audit audit )
    {
        Map<String, Object> values = new HashMap<>();
        values.put( "auditType", audit.getAuditType() );
        values.put( "auditScope", audit.getAuditScope() );
        values.put( "createdAt", audit.getCreatedAt() );
        values.put( "createdBy", audit.getCreatedBy() );
        values.put( "klass", audit.getKlass() );
        values.put( "uid", audit.getUid() );
        values.put( "code", audit.getCode() );
        values.put( "data", audit.getData() );

        return auditInsert.executeAndReturnKey( values ).longValue();
    }

    @Override
    @Transactional
    public void save( List<Audit> audits )
    {

    }

    @Override
    @Transactional
    public void delete( Audit audit )
    {
        System.err.println( "Deleting audit " + audit.getId() );
        jdbcTemplate.update( "DELETE FROM audit WHERE auditId=?", audit.getId() );
    }

    @Override
    @Transactional
    public void delete( AuditQuery query )
    {

    }

    @Override
    @Transactional( readOnly = true )
    public int count( AuditQuery query )
    {
        return 0;
    }

    @Override
    @Transactional( readOnly = true )
    public List<Audit> query( AuditQuery query )
    {
        List<Audit> audits = jdbcTemplate.query( "SELECT * FROM audit", auditRowMapper );
        return audits;
    }

    private RowMapper<Audit> auditRowMapper = ( rs, rowNum ) -> {
        Date createdAt = rs.getDate( "createdAt" );

        return Audit.builder()
            .id( rs.getLong( "auditId" ) )
            .auditType( AuditType.valueOf( rs.getString( "auditType" ) ) )
            .auditScope( AuditScope.valueOf( rs.getString( "auditScope" ) ) )
            .createdAt( new Timestamp( createdAt.getTime() ).toLocalDateTime() )
            .createdBy( rs.getString( "createdBy" ) )
            .klass( rs.getString( "klass" ) )
            .uid( rs.getString( "uid" ) )
            .code( rs.getString( "code" ) )
            .data( rs.getString( "data" ) )
            .build();
    };
}