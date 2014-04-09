package cn.ac.ict.partition.io.formats;

import java.io.IOException;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.io.formats.TextVertexInputFormat;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.log4j.Logger;

public abstract class TextVertexFromMultiLinesInputFormat<I extends WritableComparable, V extends WritableComparable, E extends WritableComparable> 
	extends	TextVertexInputFormat<I, V, E> {
	
	private static Logger LOG = Logger.getLogger(TextVertexFromMultiLinesInputFormat.class);
	
	
	protected abstract class TextVertexFromMultiLinesVertexReader extends TextVertexReader{
		
		private Vertex<I, V, E, ?> currentVertex = null;

		@Override
		public boolean nextVertex() throws IOException, InterruptedException {
			return getRecordReader().nextKeyValue();
		}

		@Override
		public Vertex<I, V, E, ?> getCurrentVertex()
				throws IOException, InterruptedException {
			Text line = getRecordReader().getCurrentValue();
			while(currentVertex.getId().equals(getId(line))){
				if(currentVertex == null){
					currentVertex = initialNextVertext(line);
				}else{
					for(Edge<I, E> e : getEdges()){
						currentVertex.addEdge(e);
					}
				}
				line = getRecordReader().getCurrentValue();
			}
			Vertex<I, V, E, ?> vertex = currentVertex;
			currentVertex = initialNextVertext(line);
			
			return vertex;
		}
		
		protected Vertex<I, V, E, ?> initialNextVertext(Text line){
			Vertex<I, V, E, ?> newVertex = getConf().createVertex();
			newVertex.initialize(getId(line), getValue(line), getEdges());
			return newVertex;
		}
		
		protected abstract I getId(Text line);
		
		protected abstract V getValue(Text line);
		
		protected abstract Iterable<Edge<I, E>> getEdges();
		
	}
	
protected abstract class TextVertexReaderFromMultiLinesProcessed<T> extends TextVertexReader{
		
		private Vertex<I, V, E, ?> currentVertex = null;

		@Override
		public boolean nextVertex() throws IOException, InterruptedException {
			return getRecordReader().nextKeyValue();
		}

		@Override
		public Vertex<I, V, E, ?> getCurrentVertex()
				throws IOException, InterruptedException {
			Text line = getRecordReader().getCurrentValue();
			T processed = preprocessLine(line);
			while(currentVertex.getId().equals(getId(processed))){
				if(currentVertex == null){
					currentVertex = initialNextVertext(processed);
				}else{
					for(Edge<I, E> e : getEdges()){
						currentVertex.addEdge(e);
					}
				}
				line = getRecordReader().getCurrentValue();
			}
			Vertex<I, V, E, ?> vertex = currentVertex;
			currentVertex = initialNextVertext(processed);
			
			return vertex;
		}
		
		protected Vertex<I, V, E, ?> initialNextVertext(T line){
			Vertex<I, V, E, ?> newVertex = getConf().createVertex();
			newVertex.initialize(getId(line), getValue(line), getEdges());
			return newVertex;
		}
		
		protected abstract T preprocessLine(Text line);
		
		protected abstract I getId(T line);
		
		protected abstract V getValue(T line);
		
		protected abstract Iterable<Edge<I, E>> getEdges();
		
	}

protected abstract class TextVertexReaderFromMultiLinesProcessedHandingExceptions<T, X extends Throwable> extends TextVertexReader{
	
	private boolean hasNext = true;
	private Vertex<I, V, E, ?> currentVertex = null;

	@Override
	public boolean nextVertex() throws IOException, InterruptedException {
		if(currentVertex == null && !hasNext){
			return false;
		}
		return true;
	}

	@Override
	public Vertex<I, V, E, ?> getCurrentVertex()
			throws IOException, InterruptedException {
		Text line = null;
		T processed = null;
		Vertex<I, V, E, ?> vertex = null;
		try{
			while(hasNext  = getRecordReader().nextKeyValue()){
				line = getRecordReader().getCurrentValue();
				processed = preprocessLine(line);
//				LOG.getLogger("debugLogger").info("input format: "+line+":"+((String[]) processed)[0]+((String[]) processed)[1]);
				if(currentVertex == null){
					currentVertex = initialNextVertext(processed);
					LOG.getLogger("debugLogger").info("input format(cv is null): "+line+":"+currentVertex.toString());
					continue;
				}
				if(!currentVertex.getId().equals(getId(processed))){
					break;
				}
				for(Edge<I, E> e : getEdges(processed)){
					currentVertex.addEdge(e);
				}
			}
			LOG.getLogger("debugLogger").info("input format: "+line+":"+currentVertex.toString());
			vertex = currentVertex;
			if(hasNext){
				currentVertex = initialNextVertext(processed);
			}else{
				currentVertex = null;
			}
		}catch(IOException e){
			throw e;
		}catch(Throwable t){
			handleException(line, processed, (X) t);
		}
		
		return vertex;
	}
	
	protected Vertex<I, V, E, ?> initialNextVertext(T line){
		Vertex<I, V, E, ?> newVertex = getConf().createVertex();
		newVertex.initialize(getId(line), getValue(line), getEdges(line));
		return newVertex;
	}
	
	protected abstract T preprocessLine(Text line) throws X, IOException;
	
	protected abstract I getId(T line);
	
	protected abstract V getValue(T line);
	
	protected abstract Iterable<Edge<I, E>> getEdges(T line);
	
	protected Vertex<I, V, E, ?> handleException(Text line, T processed, X t){
		throw new IllegalArgumentException(t);
	}
	
}
}
