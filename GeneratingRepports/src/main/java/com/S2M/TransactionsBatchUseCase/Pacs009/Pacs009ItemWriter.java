package com.S2M.TransactionsBatchUseCase.Pacs009;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class Pacs009ItemWriter implements ItemWriter<Pacs009Message.Pacs009MessageItem> {

    private final List<Pacs009Message.Pacs009MessageItem> itemsBuffer = new ArrayList<>();

    @Override
    public void write(Chunk<? extends Pacs009Message.Pacs009MessageItem> items) throws Exception {
        for (Pacs009Message.Pacs009MessageItem item : items) {
            itemsBuffer.add(item);
        }
    }

    public void afterJobWriteToXml() throws Exception {
        Pacs009Message message = new Pacs009Message();

        Pacs009Message.GroupHeader header = new Pacs009Message.GroupHeader(
                "MSG-" + UUID.randomUUID(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        );

        message.setGrpHdr(header);
        message.setTransactions(itemsBuffer);

        // ✅ Write to a normal folder: ./pacs009_output/
        File outputDir = new File("pacs009_output");
        outputDir.mkdirs(); // ensure the folder exists

        File outputFile = new File(outputDir, "pacs009.xml");

        JAXBContext context = JAXBContext.newInstance(Pacs009Message.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        marshaller.marshal(message, outputFile);

        log.info("✅ pacs.009 XML written to: {}", outputFile.getAbsolutePath());
    }
}
