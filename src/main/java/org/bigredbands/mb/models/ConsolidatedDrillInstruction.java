package org.bigredbands.mb.models;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.bigredbands.mb.utils.PDFStringUtils;

public class ConsolidatedDrillInstruction {
    private String[] rankLines;
    private String[] commandLines;
    private float commandOffset;

    public ConsolidatedDrillInstruction(
        List<String> ranks,
        PDFont rankFont,
        float rankFontSize,
        List<CommandPair> commands,
        PDFont commandFont,
        float commandFontSize,
        float maxWidth)
    {
        assert(ranks.size() > 0);
        assert(commands.size() > 0);

        String rankString = String.join(", ", ranks) + ":";
        String commandString = String.join(", ",
            commands.stream().map(CommandPair::toString).collect(Collectors.toList()));

        try {
            rankLines = PDFStringUtils.wordWrap(rankString, maxWidth, 0.0f, ",", rankFont, rankFontSize);
            commandOffset = PDFStringUtils.stringWidth(rankLines[rankLines.length - 1] + " ", rankFont, rankFontSize);
            commandLines = PDFStringUtils.wordWrap(commandString, maxWidth, commandOffset, ",", commandFont, commandFontSize);
        } catch (IOException e) {
            rankLines = new String[0];
            commandLines = new String[0];
            commandOffset = 0;
        }
    }

    public int getNumLines() {
        return Math.max(0, rankLines.length + commandLines.length - 1);
    }

    public String[] getRankLines() {
        return rankLines;
    }

    public String[] getCommandLines() {
        return commandLines;
    }

    public float getCommandOffset() {
        return commandOffset;
    }
}
