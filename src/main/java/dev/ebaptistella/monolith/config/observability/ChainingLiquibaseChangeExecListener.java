package dev.ebaptistella.monolith.config.observability;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.precondition.core.PreconditionContainer;

final class ChainingLiquibaseChangeExecListener implements ChangeExecListener {

    private final ChangeExecListener primary;
    private final ChangeExecListener delegate;

    ChainingLiquibaseChangeExecListener(ChangeExecListener primary, ChangeExecListener delegate) {
        this.primary = primary;
        this.delegate = delegate;
    }

    @Override
    public void willRun(Change change, ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        primary.willRun(change, changeSet, databaseChangeLog, database);
        if (delegate != null) {
            delegate.willRun(change, changeSet, databaseChangeLog, database);
        }
    }

    @Override
    public void ran(Change change, ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        if (delegate != null) {
            delegate.ran(change, changeSet, databaseChangeLog, database);
        }
        primary.ran(change, changeSet, databaseChangeLog, database);
    }

    @Override
    public void runFailed(
            ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Exception exception) {
        if (delegate != null) {
            delegate.runFailed(changeSet, databaseChangeLog, database, exception);
        }
        primary.runFailed(changeSet, databaseChangeLog, database, exception);
    }

    @Override
    public void willRun(
            ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, ChangeSet.RunStatus runStatus) {
        primary.willRun(changeSet, databaseChangeLog, database, runStatus);
        if (delegate != null) {
            delegate.willRun(changeSet, databaseChangeLog, database, runStatus);
        }
    }

    @Override
    public void ran(
            ChangeSet changeSet,
            DatabaseChangeLog databaseChangeLog,
            Database database,
            ChangeSet.ExecType execType) {
        if (delegate != null) {
            delegate.ran(changeSet, databaseChangeLog, database, execType);
        }
        primary.ran(changeSet, databaseChangeLog, database, execType);
    }

    @Override
    public void willRollback(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        primary.willRollback(changeSet, databaseChangeLog, database);
        if (delegate != null) {
            delegate.willRollback(changeSet, databaseChangeLog, database);
        }
    }

    @Override
    public void rolledBack(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        if (delegate != null) {
            delegate.rolledBack(changeSet, databaseChangeLog, database);
        }
        primary.rolledBack(changeSet, databaseChangeLog, database);
    }

    @Override
    public void preconditionFailed(
            PreconditionFailedException error, PreconditionContainer.FailOption onFail) {
        primary.preconditionFailed(error, onFail);
        if (delegate != null) {
            delegate.preconditionFailed(error, onFail);
        }
    }

    @Override
    public void preconditionErrored(
            PreconditionErrorException error, PreconditionContainer.ErrorOption onError) {
        primary.preconditionErrored(error, onError);
        if (delegate != null) {
            delegate.preconditionErrored(error, onError);
        }
    }

    @Override
    public void rollbackFailed(
            ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Exception exception) {
        if (delegate != null) {
            delegate.rollbackFailed(changeSet, databaseChangeLog, database, exception);
        }
        primary.rollbackFailed(changeSet, databaseChangeLog, database, exception);
    }
}
