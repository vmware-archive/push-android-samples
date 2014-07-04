/* Copyright (c) 2013 Pivotal Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.android.push.sample.model;

import com.google.gson.annotations.SerializedName;

public class BackEndMessageRequest {

    @SerializedName("message")
    public BackEndMessageRequestData message;

    @SerializedName("target")
    public BackEndMessageTarget target;

    public BackEndMessageRequest(String messageTitle, String messageBody, String platforms, String[] devices) {
        this.message = new BackEndMessageRequestData(messageTitle, messageBody);
        this.target = new BackEndMessageTarget(platforms, devices);
    }
}
