package com.readrz.topicalc;

import java.util.Date;

import me.akuz.core.gson.GsonSerializers;

import com.mongodb.BasicDBObject;

public final class Program3Options {

        private final BasicDBObject _dbo;
        private static final String _mongoServer      = "mongoServer";
        private static final String _mongoPort        = "mongoPort";
        private static final String _mongoDb          = "mongoDb";
        private static final String _outputDir        = "outputDir";
        private static final String _stopWordsFile    = "stopWordsFile";
        private static final String _auxTopicCount    = "auxTopicCount";
        private static final String _auxTopicBeta     = "auxTopicBeta";
        private static final String _topicCount       = "topicCount";
        private static final String _topicBeta        = "topicBeta";
        private static final String _warmupIterations = "warmupIterations";
        private static final String _sampleCount      = "sampleCount";
        private static final String _sampleGap        = "sampleGap";
        private static final String _minDateInc       = "minDateInc";
        private static final String _maxDateExc       = "maxDateExc";
        
        public Program3Options(
                        String mongoServer, int mongoPort, String mongoDb,
                        String outputDir, String stopWordsFile, 
                        int auxTopicCount, double auxTopicBeta,
                        int topicCount, double topicBeta,
                        int warmupIterations, int sampleCount, int sampleGap,
                        Date minDateInc, Date maxDateExc) {
                
                _dbo = new BasicDBObject();
                _dbo.put(_mongoServer, mongoServer);
                _dbo.put(_mongoPort, mongoPort);
                _dbo.put(_mongoDb, mongoDb);
                _dbo.put(_outputDir, outputDir);
                _dbo.put(_stopWordsFile, stopWordsFile);
                _dbo.put(_auxTopicCount, auxTopicCount);
                _dbo.put(_auxTopicBeta, auxTopicBeta);
                _dbo.put(_topicCount, topicCount);
                _dbo.put(_topicBeta, topicBeta);
                _dbo.put(_warmupIterations, warmupIterations);
                _dbo.put(_sampleCount, sampleCount);
                _dbo.put(_sampleGap, sampleGap);
                _dbo.put(_minDateInc, minDateInc);
                _dbo.put(_maxDateExc, maxDateExc);
        }
        
        public String getMongoServer() {
                return (String)_dbo.get(_mongoServer);
        }
        
        public int getMongoPort() {
                return (Integer)_dbo.get(_mongoPort);
        }
        
        public String getMongoDb() {
                return (String)_dbo.get(_mongoDb);
        }
        
        public String getOutputDir() {
                return (String)_dbo.get(_outputDir);
        }
        
        public String getStopWordsFile() {
                return (String)_dbo.get(_stopWordsFile);
        }
        
        public int getTopicCount() {
                return (Integer)_dbo.get(_topicCount);
        }
        
        public double getTopicBeta() {
                return (Double)_dbo.get(_topicBeta);
        }
        
        public int getAuxTopicCount() {
                return (Integer)_dbo.get(_auxTopicCount);
        }
        
        public double getAuxTopicBeta() {
                return (Double)_dbo.get(_auxTopicBeta);
        }
        
        public int getWarnupIterations() {
                return (Integer)_dbo.get(_warmupIterations);
        }
        
        public int getSampleCount() {
                return (Integer)_dbo.get(_sampleCount);
        }
        
        public int getSampleGap() {
                return (Integer)_dbo.get(_sampleGap);
        }
        
        public Date getMinDateInc() {
                return (Date)_dbo.get(_minDateInc);
        }
        
        public Date getMaxDateExc() {
                return (Date)_dbo.get(_maxDateExc);
        }

        @Override
        public String toString() {
                return GsonSerializers.NoHtmlEscapingPretty.toJson(_dbo);
        }
}
 