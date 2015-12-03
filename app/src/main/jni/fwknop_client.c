/*
 *****************************************************************************
 *
 * File:    fwknop_client.c
 *
 * Purpose: An implementation of an fwknop client for Android.
 *
 *  Fwknop is developed primarily by the people listed in the file 'AUTHORS'.
 *  Copyright (C) 2009-2014 fwknop developers and contributors. For a full
 *  list of contributors, see the file 'CREDITS'.
 *
 *  License (GNU General Public License):
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 *     USA
 *
 *****************************************************************************
 */
#include <jni.h>

#include "fwknop_client.h"
#include "fko.h"

/* Format error message.
 */
char *
fko_errmsg(char *msg, int res) {
    static char err_msg[MSG_BUFSIZE+1] = {0};
    snprintf(err_msg, MSG_BUFSIZE, "Error: %s: %s", msg, fko_errstr(res));
    return(err_msg);
}

/* JNI interface: constructs arguments and calls main function
*/
jstring Java_biz_incomsystems_fwknop2_SendSPA_sendSPAPacket(JNIEnv* env,
        jobject thiz)
{
    fko_ctx_t ctx;
    fwknop_options_t opts;

    int res, hmac_str_len = 0;
    short message_type;
    short digest_type = FKO_DIGEST_SHA256;
    short hmac_type = FKO_HMAC_SHA256;
    int key_len, hmac_key_len;
    char res_msg[MSG_BUFSIZE+1] = {0};
    char spa_msg[MSG_BUFSIZE+1] = {0};
    char nat_msg[MSG_BUFSIZE+1] = {0};
    jstring ourSpa;
    char *key_tmp[MAX_KEY_LEN+1] = {0}, *hmac_key_tmp[MAX_KEY_LEN+1] = {0};


    LOGV("**** Init fwknop ****");

    memset(&opts, 0, sizeof(fwknop_options_t));

    /* Read the member values from the Java Object that called sendSPAPacket() method
    */
    jclass c = (*env)->GetObjectClass(env,thiz);
    jfieldID fid = (*env)->GetFieldID(env, c, "access_str", "Ljava/lang/String;");
    jstring jaccess = (*env)->GetObjectField(env, thiz, fid);
    const char *access_str = (*env)->GetStringUTFChars(env, jaccess, 0);

    fid = (*env)->GetFieldID(env, c, "allowip_str", "Ljava/lang/String;");
    jstring jallowip = (*env)->GetObjectField(env, thiz, fid);
    const char *allowip_str = (*env)->GetStringUTFChars(env, jallowip, 0);

    fid = (*env)->GetFieldID(env, c, "passwd_str", "Ljava/lang/String;");
    jstring jpasswd = (*env)->GetObjectField(env, thiz, fid);
    char *passwd_str = (*env)->GetStringUTFChars(env, jpasswd, 0);

    fid = (*env)->GetFieldID(env, c, "passwd_b64", "Ljava/lang/String;");
    jstring jpasswd_b64 = (*env)->GetObjectField(env, thiz, fid);
    const char *passwd_b64 = (*env)->GetStringUTFChars(env, jpasswd_b64, 0);

    fid = (*env)->GetFieldID(env, c, "digest_type", "Ljava/lang/String;");
    jstring jdigest_type = (*env)->GetObjectField(env, thiz, fid);
    char *set_digest_type = (*env)->GetStringUTFChars(env, jdigest_type, 0);

    fid = (*env)->GetFieldID(env, c, "hmac_str", "Ljava/lang/String;");
    jstring jhmac = (*env)->GetObjectField(env, thiz, fid);
    char *hmac_str = (*env)->GetStringUTFChars(env, jhmac, 0);

    fid = (*env)->GetFieldID(env, c, "hmac_b64", "Ljava/lang/String;");
    jstring jhmac_b64 = (*env)->GetObjectField(env, thiz, fid);
    const char *hmac_b64 = (*env)->GetStringUTFChars(env, jhmac_b64, 0);

    fid = (*env)->GetFieldID(env, c, "hmac_type", "Ljava/lang/String;");
    jstring jhmac_type = (*env)->GetObjectField(env, thiz, fid);
    char *set_hmac_type = (*env)->GetStringUTFChars(env, jhmac_type, 0);

    fid = (*env)->GetFieldID(env, c, "fw_timeout_str", "Ljava/lang/String;");
    jstring jfwtimeout = (*env)->GetObjectField(env, thiz, fid);
    const char *fw_timeout_str = (*env)->GetStringUTFChars(env, jfwtimeout, 0);

    fid = (*env)->GetFieldID(env, c, "nat_access_str", "Ljava/lang/String;");
    jstring jnat_access_str = (*env)->GetObjectField(env, thiz, fid);
    const char *nat_access_str = (*env)->GetStringUTFChars(env, jnat_access_str, 0);

    fid = (*env)->GetFieldID(env, c, "nat_local", "Ljava/lang/String;");
    jstring jnat_local = (*env)->GetObjectField(env, thiz, fid);
    const char *nat_local = (*env)->GetStringUTFChars(env, jnat_local, 0);

    fid = (*env)->GetFieldID(env, c, "server_cmd_str", "Ljava/lang/String;");
    jstring jserver_cmd = (*env)->GetObjectField(env, thiz, fid);
    const char *server_cmd_str = (*env)->GetStringUTFChars(env, jserver_cmd, 0);

    fid = (*env)->GetFieldID(env, c, "legacy", "Ljava/lang/String;");
    jstring jlegacy = (*env)->GetObjectField(env, thiz, fid);
    const char *legacy = (*env)->GetStringUTFChars(env, jlegacy, 0);

    /* Sanity checks
    */
    if(access_str == NULL) {
        sprintf(res_msg, "Error: Invalid or missing access string");
        goto cleanup2;
    }
    if(allowip_str == NULL) {
        sprintf(res_msg, "Error: Invalid or missing allow IP");
        goto cleanup2;
    }
    if(passwd_str == NULL) {
        sprintf(res_msg, "Error: Invalid or missing password");
        goto cleanup2;
    }
    if(fw_timeout_str == NULL) {
        sprintf(res_msg, "Error: Invalid or missing firewall timeout value");
        goto cleanup2;
    }

    if(hmac_str != NULL) {
        hmac_str_len = (int)strlen(hmac_str);
    }
    key_len = (int)strlen(passwd_str);
    if(legacy == NULL) {
    sprintf(legacy, "false");
    }


    if(strcmp(hmac_b64, "true") == 0) {
        hmac_str_len = fko_base64_decode( hmac_str,
                                (unsigned char *)hmac_key_tmp);
        if(hmac_str_len > MAX_KEY_LEN || hmac_str_len < 0)
        {
            LOGV("[*] Invalid key length: '%d', must be in [1,%d]",
                    hmac_str_len, MAX_KEY_LEN);
            goto cleanup2;
        }
        else
        {
            memcpy(hmac_str, hmac_key_tmp, hmac_str_len);
        }
    }

    if(strcmp(passwd_b64, "true") == 0) {
        LOGV("Detected key b64");
        key_len = fko_base64_decode(passwd_str,
                        (unsigned char *)key_tmp);
        if(key_len > MAX_KEY_LEN || key_len < 0)
        {
            LOGV( "[*] Invalid key length: '%d', must be in [1,%d]",
                    key_len, MAX_KEY_LEN);
            goto cleanup2;
        }
        else
        {
            memcpy(passwd_str, key_tmp, key_len);
        }
    }
    /* Using an HMAC is optional in the pre-rfc mode.
    */

    if (server_cmd_str[0] != 0x0) {
        message_type = FKO_COMMAND_MSG;
    } else {
        message_type = FKO_CLIENT_TIMEOUT_NAT_ACCESS_MSG;
    }

    /* Intialize the context
    */
    res = fko_new(&ctx);
    if (res != FKO_SUCCESS) {
        strcpy(res_msg, fko_errmsg("Unable to create FKO context", res));
        goto cleanup2;
    }

    /* Set server command
        */

    if (server_cmd_str[0] != 0x0) {
        message_type = FKO_COMMAND_MSG;
        fko_set_spa_message_type(ctx, message_type);
        res = fko_set_spa_message(ctx, server_cmd_str);
            if (res != FKO_SUCCESS) {
                strcpy(res_msg, fko_errmsg("Error setting SPA request message", res));
                goto cleanup;
            }
    } else {

        /* Set client timeout
        */
        res = fko_set_spa_client_timeout(ctx, atoi(fw_timeout_str));
        if (res != FKO_SUCCESS) {
            strcpy(res_msg, fko_errmsg("Error setting FW timeout", res));
            goto cleanup;
        }

        /* Set the spa message string
        */
        snprintf(spa_msg, MSG_BUFSIZE, "%s,%s", allowip_str, access_str);

        res = fko_set_spa_message(ctx, spa_msg);
        if (res != FKO_SUCCESS) {
            strcpy(res_msg, fko_errmsg("Error setting SPA request message", res));
            goto cleanup;
        }
    }

    /* Set the HMAC mode if necessary
    */
    if (strcmp(legacy, "true") == 0) {
        res = fko_set_spa_encryption_mode(ctx, FKO_ENC_MODE_CBC_LEGACY_IV);
        if (key_len > 16) {
            key_len = 16;
        }
    }


    if (hmac_str_len > 0) {
        if (strcmp(set_hmac_type, "MD5") == 0) {
            hmac_type = FKO_HMAC_MD5;
        } else if (strcmp(set_hmac_type, "SHA1") == 0) {
            hmac_type = FKO_HMAC_SHA1;
        } else if (strcmp(set_hmac_type, "SHA256") == 0) {
            hmac_type = FKO_HMAC_SHA256;
        } else if (strcmp(set_hmac_type, "SHA384") == 0) {
            hmac_type = FKO_HMAC_SHA384;
        } else if (strcmp(set_hmac_type, "SHA512") == 0) {
            hmac_type = FKO_HMAC_SHA512;
        }

        res = fko_set_spa_hmac_type(ctx, hmac_type);
        if (res != FKO_SUCCESS) {
            strcpy(res_msg, fko_errmsg("Error setting SPA HMAC type", res));
            goto cleanup;
        }
    }



    /* Set Nat
    */
    if (nat_access_str[0] != 0x0){
        // if nat_access_str is not blank, push it into fko context
        if (strncmp(nat_local, "true", 4) == 0) {
            message_type = FKO_CLIENT_TIMEOUT_LOCAL_NAT_ACCESS_MSG;
            fko_set_spa_message_type(ctx, message_type);
            LOGV("Finished setting local-nat.");
        }
        res = fko_set_spa_nat_access(ctx, nat_access_str);
        if (res != FKO_SUCCESS) {
                    strcpy(res_msg, fko_errmsg("Error setting NAT string", res));
                    goto cleanup;
                }
    }
    LOGV("Setting digest type to %s.", set_digest_type);
    if (strcmp(set_digest_type, "MD5") == 0) {
        digest_type = FKO_HMAC_MD5;
    } else if (strcmp(set_digest_type, "SHA1") == 0) {
        digest_type = FKO_HMAC_SHA1;
    } else if (strcmp(set_digest_type, "SHA256") == 0) {
        digest_type = FKO_HMAC_SHA256;
    } else if (strcmp(set_digest_type, "SHA384") == 0) {
        digest_type = FKO_HMAC_SHA384;
    } else if (strcmp(set_digest_type, "SHA512") == 0) {
        digest_type = FKO_HMAC_SHA512;
    }
    res = fko_set_spa_digest_type(ctx, digest_type);
    if (res != FKO_SUCCESS) {
        strcpy(res_msg, fko_errmsg("Error setting SPA digest type", res));
        goto cleanup;
    }
    LOGV("Finished setting digest type.");


    /* Finalize the context data (Encrypt and encode).
    */
    res = fko_spa_data_final(ctx, (char*)passwd_str,
            key_len, (char *)hmac_str, hmac_str_len);
    if (res != FKO_SUCCESS) {
        strcpy(res_msg, fko_errmsg("Error generating SPA data", res));
        goto cleanup;
    }
    LOGV("Finished finalize.");

    res = fko_get_spa_data(ctx, &opts.spa_data);
    if (res != FKO_SUCCESS) {
        strcpy(res_msg, fko_errmsg("Error getting SPA data", res));
        goto cleanup;
    }


    /* Generate the spa data packet
    */
    ourSpa = (*env)->NewStringUTF(env, opts.spa_data);

cleanup:
    /* Release the resources used by the fko context.
    */
    fko_destroy(ctx);

cleanup2:
    /* Release mem
    */
    (*env)->ReleaseStringUTFChars(env, jaccess, access_str);
    (*env)->ReleaseStringUTFChars(env, jallowip, allowip_str);
    (*env)->ReleaseStringUTFChars(env, jpasswd, passwd_str);
    (*env)->ReleaseStringUTFChars(env, jpasswd_b64, passwd_b64);
    (*env)->ReleaseStringUTFChars(env, jdigest_type, set_digest_type);
    (*env)->ReleaseStringUTFChars(env, jhmac, hmac_str);
    (*env)->ReleaseStringUTFChars(env, jhmac_b64, hmac_b64);
    (*env)->ReleaseStringUTFChars(env, jhmac_type, set_hmac_type);
    (*env)->ReleaseStringUTFChars(env, jfwtimeout, fw_timeout_str);
    (*env)->ReleaseStringUTFChars(env, jnat_access_str, nat_access_str);
    (*env)->ReleaseStringUTFChars(env, jnat_local, nat_local);
    return ourSpa;
}

/***EOF***/