/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package benchmark.rpc.thrift;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;

/**
 * A TServiceClient is used to communicate with a TService implementation across
 * protocols and transports.
 */
public abstract class TServiceClientNoPrint extends org.apache.thrift.TServiceClient {

	public TServiceClientNoPrint(TProtocol prot) {
		super(prot);
	}

	public TServiceClientNoPrint(TProtocol iprot, TProtocol oprot) {
		super(iprot, oprot);
	}

	@Override
	protected void receiveBase(TBase<?, ?> result, String methodName) throws TException {
		TMessage msg = iprot_.readMessageBegin();
		if (msg.type == TMessageType.EXCEPTION) {
			TApplicationException x = new TApplicationException();
			x.read(iprot_);
			iprot_.readMessageEnd();
			throw x;
		}
		// System.out.format("Received %d%n", msg.seqid);
		if (msg.seqid != seqid_) {
			throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, String.format(
					"%s failed: out of sequence response: expected %d but got %d", methodName, seqid_, msg.seqid));
		}
		result.read(iprot_);
		iprot_.readMessageEnd();
	}

}
