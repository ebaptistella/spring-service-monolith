package dev.ebaptistella.monolith.config.observability;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.precondition.core.PreconditionContainer;

final class ObservingLiquibaseChangeExecListener implements ChangeExecListener {

    private final ObservationRegistry observationRegistry;
    private final ThreadLocal<Observation.Scope> activeScope = new ThreadLocal<>();

    ObservingLiquibaseChangeExecListener(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    @Override
    public void willRun(Change change, ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        String changeType = change.getClass().getSimpleName();
        Observation observation = Observation.createNotStarted("liquibase.change", observationRegistry)
                .contextualName(changeSet.getId() + ":" + changeType)
                .lowCardinalityKeyValue(KeyValue.of("liquibase.changeSet", changeSet.getId()))
                .lowCardinalityKeyValue(KeyValue.of("liquibase.change", changeType))
                .start();
        activeScope.set(observation.openScope());
    }

    @Override
    public void ran(Change change, ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        closeScope();
    }

    @Override
    public void runFailed(
            ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Exception exception) {
        closeScope();
    }

    @Override
    public void rollbackFailed(
            ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Exception exception) {
        closeScope();
    }

    @Override
    public void willRun(
            ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, ChangeSet.RunStatus runStatus) {
        // changeset-level events are not observed to keep startup traces readable
    }

    @Override
    public void ran(
            ChangeSet changeSet,
            DatabaseChangeLog databaseChangeLog,
            Database database,
            ChangeSet.ExecType execType) {
        // noop
    }

    @Override
    public void willRollback(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        // noop
    }

    @Override
    public void rolledBack(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        // noop
    }

    @Override
    public void preconditionFailed(
            PreconditionFailedException error, PreconditionContainer.FailOption onFail) {
        // noop
    }

    @Override
    public void preconditionErrored(
            PreconditionErrorException error, PreconditionContainer.ErrorOption onError) {
        // noop
    }

    private void closeScope() {
        Observation.Scope scope = activeScope.get();
        if (scope != null) {
            scope.close();
            activeScope.remove();
        }
    }
}
