package com.cloudera.sa.hcu.io.put.hdfs.writer;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CreateFlag;
import org.apache.hadoop.fs.FileContext;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import com.cloudera.sa.hcu.utils.PropertyUtils;

public class SequenceFileDelimiterWriter extends AbstractWriter
{
	private static final String CONF_DELIMITER = "writer.delimiter";
	private static final String CONF_COMPRESSION_CODEC = COMPRESSION_CODEC;
	
	SequenceFile.Writer writer;
	String regexDelimiter;
	Text value = new Text();
	
	public SequenceFileDelimiterWriter(Properties p) throws Exception
	{
		super(p);
	}
	
	public SequenceFileDelimiterWriter(String outputPath, String regexDelimiter, String compressionCodec) throws IOException
	{
		super( makeProperties(outputPath, regexDelimiter, compressionCodec));
	}
	
	private static Properties makeProperties(String outputPath, String regexDelimiter, String compressionCodec)
	{
		Properties p = new Properties();
		
		p.setProperty(CONF_OUTPUT_PATH, outputPath);
		p.setProperty(CONF_DELIMITER, regexDelimiter);
		p.setProperty(CONF_COMPRESSION_CODEC, compressionCodec);
		
		return p;
	}
	
	@Override
	protected void init(String outputPath, Properties p) throws IOException
	{
		this.regexDelimiter = PropertyUtils.getStringProperty(p, CONF_DELIMITER);
		
		//Open hdfs file system
		Configuration config = new Configuration();
		
		//Create path object
		System.out.println("Creating '" + outputPath + "'");
		
		Path outputFilePath = new Path(outputPath);

		//Created our writer
		SequenceFile.Metadata metaData = new SequenceFile.Metadata();
	
		EnumSet<CreateFlag> enumSet = EnumSet.of(CreateFlag.CREATE);
		writer = SequenceFile.createWriter( FileContext.getFileContext(), config, outputFilePath, NullWritable.class, Text.class, SequenceFile.CompressionType.BLOCK, PropertyUtils.getCompressionCodecProperty(p, CONF_COMPRESSION_CODEC), metaData, enumSet);
	}
	
	public void writeRow(String rowType, String[] columns) throws IOException
	{
		StringBuilder strBuilder = new StringBuilder();
		for (String column: columns)
		{
			strBuilder.append(column + regexDelimiter);
		}
		strBuilder.delete(strBuilder.length() - regexDelimiter.length(), strBuilder.length());
		
		value.set(strBuilder.toString());
		
		writer.append(NullWritable.get(), value);
	}

	public void close() throws IOException
	{
		writer.close();
	}


}
