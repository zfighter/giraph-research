/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.giraph.conf;

import org.apache.hadoop.conf.Configuration;

/**
 * Long configuration option
 */
public class LongConfOption extends AbstractConfOption {
  /** Default value */
  private long defaultValue;

  /**
   * Constructor
   * @param key key
   * @param defaultValue default value
   */
  public LongConfOption(String key, long defaultValue) {
    super(key);
    this.defaultValue = defaultValue;
    AllOptions.add(this);
  }

  public long getDefaultValue() {
    return defaultValue;
  }

  @Override public String getDefaultValueStr() {
    return Long.toString(defaultValue);
  }

  @Override public ConfOptionType getType() {
    return ConfOptionType.LONG;
  }

  /**
   * Lookup value
   * @param conf Configuration
   * @return value set for key, or defaultValue
   */
  public long get(Configuration conf) {
    return conf.getLong(getKey(), defaultValue);
  }

  /**
   * Set value for key
   * @param conf Configuration
   * @param value to set
   */
  public void set(Configuration conf, long value) {
    conf.setLong(getKey(), value);
  }

  /**
   * Set value if it's not already present
   * @param conf Configuration
   * @param value to set
   */
  public void setIfUnset(Configuration conf, long value) {
    if (conf.get(getKey()) == null) {
      conf.setLong(getKey(), value);
    }
  }
}
