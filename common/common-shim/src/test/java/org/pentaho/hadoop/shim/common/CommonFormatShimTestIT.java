package org.pentaho.hadoop.shim.common;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.junit.Test;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.hadoop.shim.api.format.PentahoRecordWriter;
import org.pentaho.hadoop.shim.api.format.RecordReader;
import org.pentaho.hadoop.shim.common.format.PentahoParquetInputFormat;
import org.pentaho.hadoop.shim.common.format.PentahoParquetOutputFormat;
import org.pentaho.hadoop.shim.common.format.SchemaDescription;
import org.pentaho.hadoop.shim.common.fs.FileSystemProxy;
import org.pentaho.hdfs.vfs.HadoopFileSystemImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by Vasilina_Terehova on 7/27/2017.
 */
public class CommonFormatShimTestIT {

  @Test
  public void testParquetReadSuccessLocalFileSystem() throws IOException, InterruptedException {
    ConfigurationProxy jobConfiguration = new ConfigurationProxy();
    jobConfiguration.set( FileInputFormat.INPUT_DIR,
      CommonFormatShimTestIT.class.getClassLoader().getResource( "sample.pqt" ).getFile() );
    SchemaDescription schemaDescription = makeScheme();
    String schemaString = "message PersonRecord {\n"
      + "required binary name;\n"
      + "required binary age;\n"
      + "}";
    PentahoParquetInputFormat pentahoParquetInputFormat =
      new PentahoParquetInputFormat( jobConfiguration, schemaDescription,
        new FileSystemProxy( FileSystem.get( jobConfiguration ) ) );
    RecordReader recordReader =
      pentahoParquetInputFormat.getRecordReader( pentahoParquetInputFormat.getSplits().get( 0 ) );
    recordReader.forEach( rowMetaAndData -> {
      RowMetaInterface rowMeta = rowMetaAndData.getRowMeta();
      for ( String fieldName : rowMeta.getFieldNames() ) {
        try {
          System.out.println( fieldName + " " + rowMetaAndData.getString( fieldName, "" ) );
        } catch ( KettleValueException e ) {
          e.printStackTrace();
        }
      }
    } );

  }

  @Test
  public void testParquetReadSuccessHdfsFileSystem() throws IOException, InterruptedException {
    ConfigurationProxy jobConfiguration = new ConfigurationProxy();
    SchemaDescription schemaDescription = makeScheme();
    jobConfiguration
      .set( FileInputFormat.INPUT_DIR, "hdfs://svqxbdcn6cdh510n1.pentahoqa.com:8020/user/devuser/parquet" );
    PentahoParquetInputFormat pentahoParquetInputFormat =
      new PentahoParquetInputFormat( jobConfiguration, schemaDescription,
        new FileSystemProxy( new HadoopFileSystemImpl( FileSystem.get( jobConfiguration ) ) ) );
    RecordReader recordReader =
      pentahoParquetInputFormat.getRecordReader( pentahoParquetInputFormat.getSplits().get( 0 ) );
    recordReader.forEach( rowMetaAndData -> {
      RowMetaInterface rowMeta = rowMetaAndData.getRowMeta();
      for ( String fieldName : rowMeta.getFieldNames() ) {
        try {
          System.out.println( fieldName + " " + rowMetaAndData.getString( fieldName, "" ) );
        } catch ( KettleValueException e ) {
          e.printStackTrace();
        }
      }
    } );

  }


  @Test
  public void testParquetReadSuccessLocalFileSystemAlex() throws IOException, InterruptedException {
    try {
      SchemaDescription schemaDescription = makeScheme();

      ConfigurationProxy jobConfiguration = new ConfigurationProxy();
      jobConfiguration.set( FileInputFormat.INPUT_DIR, CommonFormatShimTestIT.class.getClassLoader().getResource(
        "sample.pqt" ).getFile() );
      PentahoParquetInputFormat pentahoParquetInputFormat =
        new PentahoParquetInputFormat( jobConfiguration, schemaDescription, new FileSystemProxy( FileSystem.get(
          jobConfiguration ) ) );
      RecordReader recordReader =
        pentahoParquetInputFormat.getRecordReader( pentahoParquetInputFormat.getSplits().get( 0 ) );
      recordReader.forEach( rowMetaAndData -> {
        RowMetaInterface rowMeta = rowMetaAndData.getRowMeta();
        for ( String fieldName : rowMeta.getFieldNames() ) {
          try {
            System.out.println( fieldName + " " + rowMetaAndData.getString( fieldName, "" ) );
          } catch ( KettleValueException e ) {
            e.printStackTrace();
          }
        }
      } );
    } catch ( Throwable ex ) {
      ex.printStackTrace();
    }
  }

  @Test
  public void testParquetWriteSuccessLocalFileSystem() throws IOException, InterruptedException {
    try {
      SchemaDescription schemaDescription = makeScheme();
      Path tempFile = Files.createTempDirectory( "parquet" );

      ConfigurationProxy jobConfiguration = new ConfigurationProxy();
      jobConfiguration.set( FileOutputFormat.OUTDIR, tempFile.toString() );
      PentahoParquetOutputFormat pentahoParquetOutputFormat =
        new PentahoParquetOutputFormat( jobConfiguration, schemaDescription );
      PentahoRecordWriter recordWriter =
        pentahoParquetOutputFormat.getRecordWriter();
      RowMetaAndData
        row = new RowMetaAndData();
      RowMeta rowMeta = new RowMeta();
      rowMeta.addValueMeta( new ValueMetaString( "Name" ) );
      rowMeta.addValueMeta( new ValueMetaString( "Age" ) );
      row.setRowMeta( rowMeta );
      row.setData( new Object[] { "Andrey", "11 years" } );

      //for now integer doesn't work! for read
      recordWriter.write( row );
      recordWriter.close();
    } catch ( Throwable ex ) {
      ex.printStackTrace();
    }
  }

  private SchemaDescription makeScheme() {
    SchemaDescription s = new SchemaDescription();
    s.addField( s.new Field( "b", "Name", ValueMetaInterface.TYPE_STRING ) );
    s.addField( s.new Field( "c", "Age", ValueMetaInterface.TYPE_STRING ) );
    return s;
  }
}