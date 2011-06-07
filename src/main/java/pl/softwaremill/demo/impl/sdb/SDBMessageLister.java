package pl.softwaremill.demo.impl.sdb;

import com.xerox.amazonws.simpledb.Item;
import com.xerox.amazonws.simpledb.SDBException;
import org.joda.time.format.ISODateTimeFormat;
import pl.softwaremill.demo.entity.Message;
import pl.softwaremill.demo.service.MessagesLister;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static pl.softwaremill.demo.MessageMappingConstants.*;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class SDBMessageLister implements MessagesLister {
    private final MessagesDomainProvider messagesDomainProvider;

    public SDBMessageLister(MessagesDomainProvider messagesDomainProvider) {
        this.messagesDomainProvider = messagesDomainProvider;
    }

    @Override
    public List<Message> listRecentMessages(String room) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ").append(MESSAGES_DOMAIN).append(" WHERE ")
                .append(ROOM).append(" = '").append(room).append("' AND ").append(DATE)
                .append(" IS NOT NULL ORDER BY ").append(DATE).append(" DESC LIMIT 100");

        String query = sb.toString();
        System.out.println("Executing query: " + query);

        List<Item> items;
        try {
            items = messagesDomainProvider.getDomain().selectItems(query, null, false).getItems();
        } catch (SDBException e) {
            throw new RuntimeException(e);
        }
        List<Message> messages = new ArrayList<Message>();

        for (Item item : items) {
            messages.add(convertItemToMessage(item));
        }

        return messages;
    }

    private Message convertItemToMessage(Item item) {
        return new Message(
                UUID.fromString(item.getIdentifier()),
                item.getAttribute(ROOM),
                item.getAttribute(CONTENT),
                ISODateTimeFormat.dateTimeNoMillis().parseDateTime(item.getAttribute(DATE)),
                ISODateTimeFormat.dateTimeNoMillis().parseDateTime(item.getAttribute(SAVE_DATE))
        );
    }
}
