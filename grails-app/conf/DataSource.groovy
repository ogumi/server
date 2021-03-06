/*
 * Copyright (c) 2015 naymspace software (Dennis Nissen)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

dataSource {
	pooled = true
	driverClassName = "org.mariadb.jdbc.Driver"
	dialect = "org.hibernate.dialect.MySQL5InnoDBDialect"
	url = "jdbc:mariadb://localhost/myDatabase?useUnicode=yes&characterEncoding=UTF-8"
	username = ""
	password = ""
	properties {
		jmxEnabled = true
		initialSize = 5
		maxActive = 50
		minIdle = 5
		maxIdle = 25
		maxWait = 10000
		maxAge = 10 * 60000
		timeBetweenEvictionRunsMillis = 5000
		minEvictableIdleTimeMillis = 60000
		validationQuery = "SELECT 1"
		validationQueryTimeout = 3
		validationInterval = 15000
		testOnBorrow = true
		testWhileIdle = true
		testOnReturn = false
		ignoreExceptionOnPreLoad = true
		jdbcInterceptors = "ConnectionState;StatementCache(max=200)"
		defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_COMMITTED
		dbProperties {
			autoReconnect=false
			jdbcCompliantTruncation=false
			zeroDateTimeBehavior='convertToNull'
			cachePrepStmts=false
			cacheCallableStmts=false
			dontTrackOpenResources=true
			holdResultsOpenOverStatementClose=true
			useServerPrepStmts=false
			cacheServerConfiguration=true
			cacheResultSetMetadata=true
			metadataCacheSize=100
			connectTimeout=15000
			socketTimeout=120000
			maintainTimeStats=false
			enableQueryTimeouts=false
			noDatetimeStringSync=true
		}
	}
}
hibernate {
	cache.use_second_level_cache = true
	cache.use_query_cache = false
	cache.region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory'
	singleSession = true
	flush.mode = 'manual'
}

// environment specific settings
environments {
	development {
			dataSource {
                driverClassName = "org.h2.Driver"
                url = "jdbc:h2:mem:devDb;DB_CLOSE_DELAY=-1"
				dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
			}
			hibernate {
				show_sql = false
			}
	}
	test {
		dataSource {
			dbCreate = "update"
		}
	}
	production {
		dataSource {
			dbCreate = "update"
		}
	}
}
