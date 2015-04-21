package fu.berlin.csw.dbpedia.client.ui.portlet;

import java.io.Serializable;

/**
 * Author: Lars Parmakerli<br>
 * Freie Universität Berlin<br>
 * corporate semantic web<br>
 * Date: 14/08/2014
 */

public class Message implements Serializable {

   private static final long serialVersionUID = -2036532075794828641L;
   private String message;
   public Message(){};

   public void setMessage(String message) {
      this.message = message;
   }

   public String getMessage() {
      return message;
   }
}