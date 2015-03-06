/*
 * #%L
 * Layer7 Monitor
 * %%
 * Copyright (C) 2010 - 2015 Team Applicatie Integratie (Gemeente Den Haag)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package nl.denhaag.twb.layer7.policy;


import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import org.apache.log4j.Logger;

public class Base64Extension extends ExtensionFunctionDefinition {
		/**
		 * 
		 */
		private static final long serialVersionUID = 654874379518388994L;
		private static final StructuredQName funcname = new StructuredQName("tw", "http://www.denhaag.nl/xslt/extensions/tw",
				"base64Extension");
		private static final Logger LOG = Logger.getLogger(Base64Extension.class);
		private Base64ExtensionCall base64ExtensionCall;

		public Base64Extension() {
			this.base64ExtensionCall = new Base64ExtensionCall();
		}

		@Override
		public StructuredQName getFunctionQName() {
			return funcname;
		}

		@Override
		public int getMinimumNumberOfArguments() {
			return 1;
		}

		public int getMaximumNumberOfArguments() {
			return 1;
		}

		@Override
		public SequenceType[] getArgumentTypes() {
			return new SequenceType[] { SequenceType.OPTIONAL_STRING };
		}

		@Override
		public SequenceType getResultType(SequenceType[] sequenceTypes) {
			return SequenceType.OPTIONAL_STRING;
		}

		@Override
		public ExtensionFunctionCall makeCallExpression() {
			return base64ExtensionCall;
		}

		class Base64ExtensionCall extends ExtensionFunctionCall {
			private static final long serialVersionUID = 6761914863093344493L;


			public SequenceIterator call(SequenceIterator[] arguments, XPathContext context) throws XPathException {
				if (arguments.length == 1) {
					String value = arguments[0].next().getStringValue();
					value = new String(Base64.decode(value));		
					value = value.replaceAll("[\n\r\t]", ", ");
					return SingletonIterator.makeIterator(new StringValue(value));
				} else {
					return SingletonIterator.makeIterator(new StringValue("ERROR"));
				}
			}
		}
	

}
