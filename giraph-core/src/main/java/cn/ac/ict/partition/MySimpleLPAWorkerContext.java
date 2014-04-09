package cn.ac.ict.partition;

import org.apache.giraph.worker.WorkerContext;
import org.apache.log4j.Logger;

public class MySimpleLPAWorkerContext extends WorkerContext {

	private static Logger LOG = Logger.getLogger("tracerLogger");


	@Override
	public void preApplication() throws InstantiationException,
			IllegalAccessException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postApplication() {
		// TODO Auto-generated method stub

	}

	@Override
	public void preSuperstep() {
		// TODO Auto-generated method stub
		LOG.info(String.format("%1$02d", getSuperstep())+": ");
	}

	@Override
	public void postSuperstep() {
		// TODO Auto-generated method stub
		LOG.info("\n");
	}

}
