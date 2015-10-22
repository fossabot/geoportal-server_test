/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.framework.robots;

import com.esri.gpt.framework.util.Val;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.logging.Logger;

/**
 * Robots txt utility class/shortcut methods
 */
public final class BotsUtils {
  private static final Logger LOG = Logger.getLogger(BotsUtils.class.getName());
  
  /**
   * Gets default parser
   * @return default parser (never <code>null</code>)
   */
  public static BotsParser parser() {
    return BotsParser.getDefaultInstance();
  }
  
  /**
   * Reads robots.txt
   * @param mode robots.txt mode
   * @param serverUrl url of the server which is expected to have robots.txt
   * present
   * @return instance of {@link Bots} or <code>null</code> if unable to
   * obtain robots.txt
   */
  public static Bots readBots(BotsMode mode, String serverUrl) {
    return parser().readRobotsTxt(mode, serverUrl);
  }
  
  /**
   * Reads robots.txt
   * @param mode robots.txt mode
   * @param serverUrl url of the server which is expected to have robots.txt
   * present
   * @return instance of {@link Bots} or <code>null</code> if unable to
   * obtain robots.txt
   */
  public static Bots readBots(BotsMode mode, URL serverUrl) {
    return parser().readRobotsTxt(mode, serverUrl);
  }

  /**
   * Check if access to the particular url is allowed.
   * @param bots robots.txt
   * @param path path
   * @return <code>true</code> if access is allowed
   */
  public static boolean hasAccess(Bots bots, String path) {
    if (bots!=null) {
      Access access = bots.findAccess(path);
      if (access!=null) {
        return access.hasAccess();
      }
    }
    return true;
  }
  
  public static String transformUrl(Bots bots, String url) {
    if (bots!=null) {
      String orgUrl = url;
      ProtocolHostPort php = parseHostAttribute(bots.getHost());
      url = updateUrl(url, php);
      if (!url.equals(orgUrl)) {
        LOG.fine(String.format("Url updated from %s to %s", orgUrl, url));
      }
    }
    return url;
  }
  
  /**
   * Decodes URL octets except %2f (i.e. / character)
   * @param str string to encode
   * @return encoded string
   */
  /*package*/ static String decode(String str) throws UnsupportedEncodingException {
    if (str!=null) {
      StringBuilder sb = new StringBuilder();
      for (int idx = str.indexOf("%2f"); idx>=0; idx = str.indexOf("%2f")) {
        sb.append(str.substring(0, idx)).append("%2f");
        str = str.substring(idx+3);
      }
      sb.append(str);
      str = sb.toString();
    }
    return str;
  }
  
  private static String updateUrl(String url, ProtocolHostPort php) {
    if (url!=null && php!=null) {
      try {
        URL u = new URL(url);
        url = new URL(
                // update protocol if requested protocol not null
                php.protocol!=null? php.protocol: u.getProtocol(),
                // update host if requested host not null
                php.host!=null? php.host: u.getHost(),
                // update port ONLY if requested HOST not null
                php.host!=null? php.port!=null? php.port: -1: u.getPort(),
                u.getPath()
        ).toExternalForm();
      } catch (MalformedURLException ex) {

      }
    }
    return url;
  }
  
  private static ProtocolHostPort parseHostAttribute(String host) {
    host = Val.chkStr(host);
    if (!host.isEmpty()) {
      // parse protocol
      String protocolPart = null;
      int protocolStopIdx = host.indexOf("://");
      if (protocolStopIdx>=0) {
        protocolPart = protocolStopIdx>0? host.substring(0,protocolStopIdx): null;
        host = host.substring(protocolStopIdx+"://".length());
      }
      
      // parse host:port
      String hostPart = null;
      Integer portPart = null;
      if (!host.isEmpty()) {
        int hostStopIdx = host.indexOf(":");
        if (hostStopIdx<0) {
          hostPart = host;
        } else {
          hostPart = hostStopIdx>0? host.substring(0, hostStopIdx): null;
          try {
            portPart = Integer.parseInt(host.substring(hostStopIdx+":".length()));
          } catch (NumberFormatException ex) {
            
          }
        }
      }
      
      if (protocolPart!=null || hostPart!=null || portPart!=null) {
        return new ProtocolHostPort(protocolPart, hostPart, portPart);
      }
    }
    return null;
  }
  
  private static class ProtocolHostPort {
    String  protocol;
    String  host;
    Integer port;

    public ProtocolHostPort(String protocol, String host, Integer port) {
      this.protocol = protocol;
      this.host = host;
      this.port = port;
    }
  }
  
}
