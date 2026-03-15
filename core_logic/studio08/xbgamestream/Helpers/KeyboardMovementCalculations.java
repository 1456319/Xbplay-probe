package com.studio08.xbgamestream.Helpers;

import android.util.Log;
import com.amazon.a.a.o.c.a.b;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.language.Soundex;
/* loaded from: /app/base.apk/classes3.dex */
public class KeyboardMovementCalculations {
    private Character destination;
    private String keyboardType;
    private Character start;
    final char[][] netflixKeyboard = {new char[]{'a', 'b', 'c', 'd', 'e', 'f'}, new char[]{'g', 'h', 'i', 'j', 'k', 'l'}, new char[]{'m', 'n', 'o', 'p', 'q', 'r'}, new char[]{'s', 't', 'u', 'v', 'w', 'x'}, new char[]{'y', 'z', '1', '2', '3', '4'}, new char[]{'5', '6', '7', '8', '9', '0'}};
    final char[][] disneyKeyboard = {new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g'}, new char[]{'h', 'i', 'j', 'k', 'l', 'm', 'n'}, new char[]{'o', 'p', 'q', 'r', 's', 't', 'u'}, new char[]{'v', 'w', 'x', 'y', 'z'}, new char[]{'1', '2', '3', '4', '5', '6', '7'}, new char[]{'8', '9', '0'}};
    final char[][] systemKeyboard = {new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'}, new char[]{'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'}, new char[]{'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', '\"'}, new char[]{'z', 'x', 'c', 'v', 'b', 'n', 'm', ';', ':', '!'}};
    final char[][] peacock = {new char[]{'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'}, new char[]{'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', '~'}, new char[]{'z', 'x', 'c', 'v', 'b', 'n', 'm', b.a, '~', '~'}};
    final char[][] vudu = {new char[]{'a', 'b', 'c', 'd', 'e', 'f'}, new char[]{'g', 'h', 'i', 'j', 'k', 'l'}, new char[]{'m', 'n', 'o', 'p', 'q', 'r'}, new char[]{'s', 't', 'u', 'v', 'w', 'x'}, new char[]{'y', 'z', '0', '1', '2', '3'}, new char[]{'4', '5', '6', '7', '8', '9'}};
    final char[][] youTube = {new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g'}, new char[]{'h', 'i', 'j', 'k', 'l', 'm', 'n'}, new char[]{'o', 'p', 'q', 'r', 's', 't', 'u'}, new char[]{'v', 'w', 'x', 'y', 'z', Soundex.SILENT_MARKER, '\''}};

    public KeyboardMovementCalculations(String str, String str2, String str3) {
        this.start = Character.valueOf(str.charAt(0));
        this.destination = Character.valueOf(str2.charAt(0));
        this.keyboardType = str3;
    }

    /* JADX WARN: Removed duplicated region for block: B:32:0x00a4 A[LOOP:0: B:31:0x00a2->B:32:0x00a4, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:37:0x00ee A[LOOP:1: B:35:0x00e8->B:37:0x00ee, LOOP_END] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public byte[][] convertPositionsToByteArray() {
        /*
            Method dump skipped, instructions count: 271
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.studio08.xbgamestream.Helpers.KeyboardMovementCalculations.convertPositionsToByteArray():byte[][]");
    }

    private List<byte[]> convertLocationIntToSequence(Integer num, boolean z) {
        String str;
        ArrayList arrayList = new ArrayList();
        if (z) {
            if (num.intValue() < 0) {
                str = "dpadDown";
            } else {
                str = "dpadUp";
            }
        } else if (num.intValue() < 0) {
            str = "dpadRight";
        } else {
            str = "dpadLeft";
        }
        Log.e("JERE", "Move Direction: " + str + " Move Quantity: " + Math.abs(num.intValue()) + " Dest: " + this.destination + " Start: " + this.start);
        for (int i = 0; i < Math.abs(num.intValue()); i++) {
            arrayList.add(Helper.convertStringButtonToByteArray(str));
        }
        return arrayList;
    }

    private Integer calcMovement(char[][] cArr, boolean z) {
        int findLocation = findLocation(cArr, this.start.charValue(), z);
        int findLocation2 = findLocation(cArr, this.destination.charValue(), z);
        if (findLocation == -1 || findLocation2 == -1) {
            return null;
        }
        return Integer.valueOf(findLocation - findLocation2);
    }

    /* JADX WARN: Code restructure failed: missing block: B:14:0x0016, code lost:
        r1 = r1 + 1;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private int findLocation(char[][] r6, char r7, boolean r8) {
        /*
            r5 = this;
            r0 = 0
            r1 = r0
        L2:
            int r2 = r6.length
            if (r1 >= r2) goto L19
            r2 = r0
        L6:
            r3 = r6[r1]
            int r4 = r3.length
            if (r2 >= r4) goto L16
            char r3 = r3[r2]
            if (r3 != r7) goto L13
            if (r8 == 0) goto L12
            return r1
        L12:
            return r2
        L13:
            int r2 = r2 + 1
            goto L6
        L16:
            int r1 = r1 + 1
            goto L2
        L19:
            r6 = -1
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.studio08.xbgamestream.Helpers.KeyboardMovementCalculations.findLocation(char[][], char, boolean):int");
    }
}
