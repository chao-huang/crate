/*
 * Licensed to Crate under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.  Crate licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial
 * agreement.
 */

package io.crate.rest.action;

import io.crate.exceptions.AmbiguousColumnAliasException;
import io.crate.exceptions.AmbiguousColumnException;
import io.crate.exceptions.AnalyzerInvalidException;
import io.crate.exceptions.AnalyzerUnknownException;
import io.crate.exceptions.ColumnUnknownException;
import io.crate.exceptions.ColumnValidationException;
import io.crate.exceptions.CrateException;
import io.crate.exceptions.DuplicateKeyException;
import io.crate.exceptions.GroupByOnArrayUnsupportedException;
import io.crate.exceptions.InvalidArgumentException;
import io.crate.exceptions.InvalidColumnNameException;
import io.crate.exceptions.InvalidRelationName;
import io.crate.exceptions.InvalidSchemaNameException;
import io.crate.exceptions.LicenseViolationException;
import io.crate.exceptions.OperationOnInaccessibleRelationException;
import io.crate.exceptions.PartitionAlreadyExistsException;
import io.crate.exceptions.PartitionUnknownException;
import io.crate.exceptions.ReadOnlyException;
import io.crate.exceptions.RelationAlreadyExists;
import io.crate.exceptions.RelationUnknown;
import io.crate.exceptions.RelationValidationException;
import io.crate.exceptions.RelationsUnknown;
import io.crate.exceptions.RepositoryAlreadyExistsException;
import io.crate.exceptions.RepositoryUnknownException;
import io.crate.exceptions.ResourceUnknownException;
import io.crate.exceptions.SQLExceptions;
import io.crate.exceptions.SQLParseException;
import io.crate.exceptions.SchemaUnknownException;
import io.crate.exceptions.SnapshotAlreadyExistsException;
import io.crate.exceptions.SnapshotNameInvalidException;
import io.crate.exceptions.UnauthorizedException;
import io.crate.exceptions.UnsupportedFeatureException;
import io.crate.exceptions.UserAlreadyExistsException;
import io.crate.exceptions.UserDefinedFunctionAlreadyExistsException;
import io.crate.exceptions.UserDefinedFunctionUnknownException;
import io.crate.exceptions.UserUnknownException;
import io.crate.exceptions.ValidationException;
import io.crate.exceptions.VersioninigValidationException;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Locale;

import static io.crate.exceptions.Exceptions.userFriendlyMessageInclNested;

public class HttpError {

    private final HttpErrorStatus status;
    private final String message;

    @Nullable
    private final Throwable t;

    public HttpError(HttpErrorStatus status, String message, @Nullable Throwable t) {
        this.status = status;
        this.message = message;
        this.t = t;
    }

    public HttpErrorStatus status() {
        return status;
    }

    public String message() {
        return message;
    }

    public XContentBuilder toXContent(boolean includeErrorTrace) throws IOException {
        // @formatter:off
        XContentBuilder builder = JsonXContent.contentBuilder()
            .startObject()
            .startObject("error")
            .field("message", userFriendlyMessageInclNested(t))
            .field("code", status.errorCode())
            .endObject();
        // @formatter:on

        if (includeErrorTrace) {
            builder.field("error_trace", ExceptionsHelper.stackTrace(t));
        }
        return builder.endObject();
    }

    @Override
    public String toString() {
        return "HttpError{" +
               ", status=" + status +
               ", message='" + message + '\'' +
               ", t=" + t +
               '}';
    }

    public static HttpError fromThrowable(Throwable throwable) {
        Throwable unwrappedError = SQLExceptions.handleException(throwable, null);
        HttpErrorStatus httpErrorStatus = null;
        if (unwrappedError instanceof CrateException) {
            CrateException crateException = (CrateException) unwrappedError;
            if (crateException instanceof ValidationException) {
                if (crateException instanceof AmbiguousColumnAliasException) {
                    httpErrorStatus = HttpErrorStatus.COLUMN_ALIAS_IS_AMBIGUOUS;
                } else if (crateException instanceof AmbiguousColumnException) {
                    httpErrorStatus = HttpErrorStatus.COLUMN_ALIAS_IS_AMBIGUOUS;
                } else if (crateException instanceof AnalyzerInvalidException) {
                    httpErrorStatus = HttpErrorStatus.STATEMENT_INVALID_ANALYZER_DEFINITION;
                } else if (crateException instanceof ColumnValidationException) {
                    httpErrorStatus = HttpErrorStatus.FIELD_VALIDATION_FAILED;
                } else if (crateException instanceof GroupByOnArrayUnsupportedException) {
                    httpErrorStatus = HttpErrorStatus.FIELD_VALIDATION_FAILED;
                } else if (crateException instanceof InvalidArgumentException) {
                    httpErrorStatus = HttpErrorStatus.FIELD_VALIDATION_FAILED;
                } else if (crateException instanceof InvalidColumnNameException) {
                    httpErrorStatus = HttpErrorStatus.COLUMN_NAME_INVALID;
                } else if (crateException instanceof InvalidRelationName) {
                    httpErrorStatus = HttpErrorStatus.RELATION_INVALID_NAME;
                } else if (crateException instanceof InvalidSchemaNameException) {
                    httpErrorStatus = HttpErrorStatus.FIELD_VALIDATION_FAILED;
                } else if (crateException instanceof LicenseViolationException) {
                    httpErrorStatus = HttpErrorStatus.FIELD_VALIDATION_FAILED;
                } else if (crateException instanceof OperationOnInaccessibleRelationException) {
                    httpErrorStatus = HttpErrorStatus.FIELD_VALIDATION_FAILED;
                } else if (crateException instanceof RelationValidationException) {
                    httpErrorStatus = HttpErrorStatus.FIELD_VALIDATION_FAILED;
                } else if (crateException instanceof SQLParseException) {
                    httpErrorStatus = HttpErrorStatus.STATEMENT_INVALID_OR_UNSUPPORTED_SYNTAX;
                } else if (crateException instanceof UnsupportedFeatureException) {
                    httpErrorStatus = HttpErrorStatus.POSSIBLE_FEATURE_NOT_SUPPROTED_YET;
                } else if (crateException instanceof VersioninigValidationException) {
                    httpErrorStatus = HttpErrorStatus.FIELD_VALIDATION_FAILED;
                } else if (crateException instanceof UnauthorizedException) {
                    httpErrorStatus = HttpErrorStatus.USER_NOT_AUTHORIZED_TO_PERFORM_STATEMENT;
                } else if (crateException instanceof ReadOnlyException) {
                    httpErrorStatus = HttpErrorStatus.ONLY_READ_OPERATION_ALLOWED_ON_THIS_NODE;
                }
            } else if (crateException instanceof ResourceUnknownException) {
                if (crateException instanceof AnalyzerUnknownException) {
                    httpErrorStatus = HttpErrorStatus.STATEMENT_INVALID_ANALYZER_DEFINITION;
                } else if (crateException instanceof ColumnUnknownException) {
                    httpErrorStatus = HttpErrorStatus.COLUMN_NAME_INVALID;
                } else if (crateException instanceof PartitionUnknownException) {
                    httpErrorStatus = HttpErrorStatus.ONLY_READ_OPERATION_ALLOWED_ON_THIS_NODE;
                } else if (crateException instanceof RelationUnknown) {
                    httpErrorStatus = HttpErrorStatus.UNKNOWN_RELATION;
                } else if (crateException instanceof RelationsUnknown) {
                    httpErrorStatus = HttpErrorStatus.ONLY_READ_OPERATION_ALLOWED_ON_THIS_NODE;
                } else if (crateException instanceof RepositoryUnknownException) {
                    httpErrorStatus = HttpErrorStatus.ONLY_READ_OPERATION_ALLOWED_ON_THIS_NODE;
                } else if (crateException instanceof SchemaUnknownException) {
                    httpErrorStatus = HttpErrorStatus.ONLY_READ_OPERATION_ALLOWED_ON_THIS_NODE;
                } else if (crateException instanceof UserDefinedFunctionUnknownException) {
                    httpErrorStatus = HttpErrorStatus.ONLY_READ_OPERATION_ALLOWED_ON_THIS_NODE;
                } else if (crateException instanceof UserUnknownException) {
                    httpErrorStatus = HttpErrorStatus.ONLY_READ_OPERATION_ALLOWED_ON_THIS_NODE;
                }
            } else if (crateException instanceof DuplicateKeyException) {
                httpErrorStatus = HttpErrorStatus.DOCUMENT_WITH_THE_SAME_PRIMARY_KEY_EXISTS_ALREADY;
            } else if (crateException instanceof PartitionAlreadyExistsException) {
                httpErrorStatus = HttpErrorStatus.PARTITION_FOR_THE_SAME_VALUE_EXISTS_ALREADY;
            } else if (crateException instanceof RelationAlreadyExists) {
                httpErrorStatus = HttpErrorStatus.RELATION_WITH_THE_SAME_NAME_EXISTS_ALREADY;
            } else if (crateException instanceof RepositoryAlreadyExistsException) {
                httpErrorStatus = HttpErrorStatus.REPOSITORY_WITH_SAME_NAME_EXISTS_ALREADY;
            } else if (crateException instanceof SnapshotAlreadyExistsException) {
                httpErrorStatus = HttpErrorStatus.SNAPSHOT_WITH_SAME_NAME_EXISTS_ALREADY;
            } else if (crateException instanceof SnapshotNameInvalidException) {
                httpErrorStatus = HttpErrorStatus.CREATING_SNAPSHOT_FAILED;
            } else if (crateException instanceof UserAlreadyExistsException) {
                httpErrorStatus = HttpErrorStatus.USER_WITH_SAME_NAME_EXISTS_ALREADY;
            } else if (crateException instanceof UserDefinedFunctionAlreadyExistsException) {
                httpErrorStatus = HttpErrorStatus.USER_DEFINED_FUNCTION_WITH_SAME_SIGNATURE_EXISTS_ALREADY;
            } else {
                httpErrorStatus = HttpErrorStatus.UNHANDLED_SERVER_ERROR;
            }
        } else {
            httpErrorStatus = HttpErrorStatus.UNHANDLED_SERVER_ERROR;
        }
        String message = unwrappedError.getMessage();
        if (message == null) {
            if (throwable instanceof CrateException && throwable.getCause() != null) {
                throwable = throwable.getCause();   // use cause because it contains a more meaningful error in most cases
            }
            StackTraceElement[] stackTraceElements = throwable.getStackTrace();
            if (stackTraceElements.length > 0) {
                message = String.format(Locale.ENGLISH,
                                        "%s in %s",
                                        throwable.getClass().getSimpleName(),
                                        stackTraceElements[0]);
            } else {
                message = "Error in " + throwable.getClass().getSimpleName();
            }
        }
        return new HttpError(httpErrorStatus, message, unwrappedError);
    }
}