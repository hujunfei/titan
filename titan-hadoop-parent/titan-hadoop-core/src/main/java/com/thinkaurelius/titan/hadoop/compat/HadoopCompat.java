package com.thinkaurelius.titan.hadoop.compat;

import com.thinkaurelius.titan.diskstorage.keycolumnvalue.scan.ScanJob;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.scan.ScanMetrics;
import com.thinkaurelius.titan.graphdb.olap.VertexScanJob;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import com.thinkaurelius.titan.hadoop.config.job.JobClasspathConfigurer;

import java.io.IOException;

/**
 * This interface encapsulates both API and bytecode-level
 * IncompatibleClassChanges in Hadoop core. In theory, titan-hadoop-core should
 * only touch parts of Hadoop core ABI that have remained stable since 1.0, and
 * everything else should be hidden behind the compat layer.
 * <p>
 * This interface is public, but should be considered unstable and likely to
 * change in the future as new Hadoop versions are released, or as
 * titan-hadoop-core uses additional Hadoop features, or as bugs are discovered.
 * It's possible to write and use a third-party implementation, but be prepared
 * to update it when upgrading to a newer Titan release.
 */
public interface HadoopCompat {

    /**
     * Instantiate a new TaskAttemptContext using the given attempt ID and configuration.
     *
     * @param c configuration
     * @param t task attempt ID
     * @return new context object
     */
    public TaskAttemptContext newTask(Configuration c, TaskAttemptID t);

    /**
     * Return the Hadoop configuration key which takes a boolean value and
     * controls whether Hadoop will attempt speculative execution of mappers.
     *
     * @return string config key
     */
    public String getSpeculativeMapConfigKey();

    /**
     * Return the Hadoop configuration key which takes a boolean value and
     * controls whether Hadoop will attempt speculative execution of reducers.
     *
     * @return string config key
     */
    public String getSpeculativeReduceConfigKey();

    public String getMapredJarConfigKey();

//    public boolean runVertexScan(String vertexScanJobClass, Configuration jobConf) throws IOException, ClassNotFoundException, InterruptedException;

    /**
     * Add {@code incr} to the counter designated by {@code counter} on {@code context}.
     *
     * @param context Hadoop task IO context containing counter state
     * @param group the Hadoop counter group (heading under which the counter is displayed)
     * @param name the Hadoop counter name (the identifier for this counter within the group)
     * @param incr amount to add to the counter's current value
     */
    public void incrementContextCounter(TaskInputOutputContext context, String group, String name, long incr);

    /**
     * Return the current value of counter designated by {@code counter} on {@code context}.
     *
     * @param context Hadoop task IO context containing counter state
     * @param group the Hadoop counter group (heading under which the counter is displayed)
     * @param name the Hadoop counter name (the identifier for this counter within the group)
     * @return current counter value
     */
    public long getContextCounter(TaskInputOutputContext context, String group, String name);

    /**
     * Get configuration from the supplied task attempt context and return it.
     *
     * @param context Hadoop task attempt context
     * @return configuration on supplied {@code context}
     */
    public Configuration getContextConfiguration(TaskAttemptContext context);

    /**
     * Get configuration from the supplied job context and return it.
     *
     * @param context Hadoop job context
     * @return configuration on supplied {@code context}
     */
    public Configuration getJobContextConfiguration(JobContext context);

    /**
     * Construct a {@link com.thinkaurelius.titan.hadoop.config.job.JobClasspathConfigurer}
     * that sets the Mapreduce job jar config key to the supplied value.  The job jar
     * should contain Faunus's classes plus its entire dependency tree ("fat" jar).
     *
     * @param mapredJarPath path to the mapreduce job jar
     * @return a configurer
     */
    public JobClasspathConfigurer newMapredJarConfigurer(String mapredJarPath);

    /**
     * Construct a {@link com.thinkaurelius.titan.hadoop.config.job.JobClasspathConfigurer}
     * that walks the classpath and adds all jars its finds to the Hadoop Jobs's
     * classpaths via the Hadoop Distributed Cache.
     *
     * @return a configurer
     */
    public JobClasspathConfigurer newDistCacheConfigurer();

    /**
     * Construct a {@link org.apache.hadoop.conf.Configuration} instance which throws
     * {@link java.lang.UnsupportedOperationException} on any attempt to modify its state,
     * but which forwards to its parameter all method calls that don't mutate state (i.e. reads).
     *
     * @param base the configuration to encapsulate behind an immutable forwarder class
     * @return an immutable forwarder class that encapsulates {@code base}
     */
    public Configuration newImmutableConfiguration(Configuration base);

    public ScanMetrics getMetrics(Counters c);

    public String getJobFailureString(Job j);
}
